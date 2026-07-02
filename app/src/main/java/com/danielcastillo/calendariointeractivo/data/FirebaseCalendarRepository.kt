package com.danielcastillo.calendariointeractivo.data

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.google.android.gms.tasks.Tasks
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FirebaseCalendarRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext),
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) {

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }

        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    fun observeEvents(): Flow<List<CalendarEvent>> = callbackFlow {
        val registration = firestore
            .collection(EVENTS)
            .orderBy("startDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events: List<CalendarEvent> = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        CalendarEvent.fromDocument(document)
                    }
                    ?: emptyList()

                trySend(events)
            }

        awaitClose {
            registration.remove()
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        photoUri: Uri?
    ): AppUser {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        val result = auth
            .createUserWithEmailAndPassword(cleanEmail, password)
            .await()

        val firebaseUser = result.user ?: error("No se pudo crear el usuario.")

        val uploadedPhoto = if (photoUri != null) {
            uploadProfilePhoto(firebaseUser.uid, photoUri)
        } else {
            UploadedPhoto()
        }

        updateAuthProfile(
            firebaseUser = firebaseUser,
            name = cleanName,
            photoUrl = uploadedPhoto.downloadUrl
        )

        val appUser = AppUser(
            uid = firebaseUser.uid,
            name = cleanName,
            email = cleanEmail,
            photoUrl = uploadedPhoto.downloadUrl,
            photoStoragePath = uploadedPhoto.storagePath
        )

        firestore
            .collection(USERS)
            .document(firebaseUser.uid)
            .set(
                appUser.toProfileMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()

        afterSignIn(appUser)

        return appUser
    }

    suspend fun login(
        email: String,
        password: String
    ): AppUser {
        val result = auth
            .signInWithEmailAndPassword(email.trim(), password)
            .await()

        val firebaseUser = result.user ?: error("No se pudo iniciar sesión.")

        val appUser = ensureUserDocument(firebaseUser)

        afterSignIn(appUser)

        return appUser
    }

    suspend fun sendPasswordReset(email: String) {
        auth
            .sendPasswordResetEmail(email.trim())
            .await()

        analytics.logEvent(
            "password_reset_requested",
            Bundle().apply {
                putString("method", "email")
            }
        )
    }

    suspend fun fetchCurrentUserProfile(): AppUser? {
        val firebaseUser = auth.currentUser ?: return null
        return ensureUserDocument(firebaseUser)
    }

    suspend fun updateProfile(
        name: String,
        email: String,
        photoUri: Uri?,
        newPassword: String?
    ): AppUser {
        val firebaseUser = auth.currentUser ?: error("Sesión no iniciada.")

        val oldProfile = ensureUserDocument(firebaseUser)

        val cleanName = name.trim()
        val cleanEmail = email.trim()

        val uploadedPhoto = if (photoUri != null) {
            uploadProfilePhoto(firebaseUser.uid, photoUri)
        } else {
            UploadedPhoto(
                downloadUrl = oldProfile.photoUrl,
                storagePath = oldProfile.photoStoragePath
            )
        }

        updateAuthProfile(
            firebaseUser = firebaseUser,
            name = cleanName,
            photoUrl = uploadedPhoto.downloadUrl
        )

        if (!cleanEmail.equals(firebaseUser.email.orEmpty(), ignoreCase = true)) {
            firebaseUser
                .updateEmail(cleanEmail)
                .await()
        }

        if (!newPassword.isNullOrBlank()) {
            firebaseUser
                .updatePassword(newPassword)
                .await()
        }

        val appUser = AppUser(
            uid = firebaseUser.uid,
            name = cleanName,
            email = cleanEmail,
            photoUrl = uploadedPhoto.downloadUrl,
            photoStoragePath = uploadedPhoto.storagePath
        )

        firestore
            .collection(USERS)
            .document(firebaseUser.uid)
            .set(
                appUser.toProfileMap() + mapOf(
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()

        crashlytics.setUserId(firebaseUser.uid)
        analytics.logEvent("profile_updated", null)

        return appUser
    }

    suspend fun createEvent(draft: EventDraft): String {
        val firebaseUser = auth.currentUser ?: error("Sesión no iniciada.")
        val creator = ensureUserDocument(firebaseUser)

        val cleanTitle = draft.title.trim()
        val cleanDescription = draft.description.trim()

        if (cleanTitle.isBlank()) {
            error("El evento necesita un título.")
        }

        val normalizedEnd = if (draft.endDate.isBefore(draft.startDate)) {
            draft.startDate
        } else {
            draft.endDate
        }

        val nowMillis = System.currentTimeMillis()

        val payload = mapOf(
            "title" to cleanTitle,
            "description" to cleanDescription,
            "startDate" to draft.startDate.toIsoString(),
            "endDate" to normalizedEnd.toIsoString(),
            "colorHex" to draft.colorHex,
            "creatorUid" to creator.uid,
            "creatorName" to creator.name,
            "creatorPhotoUrl" to creator.photoUrl,
            "createdAtMillis" to nowMillis,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val document = firestore
            .collection(EVENTS)
            .add(payload)
            .await()

        analytics.logEvent(
            "calendar_event_created",
            Bundle().apply {
                putLong(
                    "event_days",
                    ChronoUnit.DAYS.between(draft.startDate, normalizedEnd) + 1L
                )
            }
        )

        crashlytics.log("Calendar event created: ${document.id}")

        return document.id
    }

    suspend fun deleteEvent(eventId: String) {
        val firebaseUser = auth.currentUser ?: error("Sesión no iniciada.")

        if (eventId.isBlank()) {
            error("No se ha podido identificar el evento.")
        }

        val documentReference = firestore
            .collection(EVENTS)
            .document(eventId)

        val document = documentReference
            .get()
            .await()

        if (!document.exists()) {
            error("El evento ya no existe.")
        }

        val creatorUid = document.getString("creatorUid").orEmpty()

        if (creatorUid != firebaseUser.uid) {
            error("Solo puede eliminar este evento la persona que lo creó.")
        }

        documentReference
            .delete()
            .await()

        analytics.logEvent(
            "calendar_event_deleted",
            Bundle().apply {
                putString("event_id", eventId)
            }
        )

        crashlytics.log("Calendar event deleted: $eventId")
    }

    suspend fun saveMessagingTokenForCurrentUser(token: String) {
        val uid = auth.currentUser?.uid ?: return
        saveMessagingToken(uid, token)
    }

    suspend fun refreshMessagingTokenForCurrentUser() {
        val uid = auth.currentUser?.uid ?: return

        val token = messaging
            .token
            .await()

        saveMessagingToken(uid, token)
    }

    fun logout() {
        analytics.logEvent("logout", null)
        auth.signOut()
    }

    private suspend fun afterSignIn(appUser: AppUser) {
        crashlytics.setUserId(appUser.uid)

        analytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, "email")
            }
        )

        runCatching {
            refreshMessagingTokenForCurrentUser()
        }.onFailure { error ->
            crashlytics.recordException(error)
        }
    }

    private suspend fun updateAuthProfile(
        firebaseUser: FirebaseUser,
        name: String,
        photoUrl: String
    ) {
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(
                photoUrl
                    .takeIf { it.isNotBlank() }
                    ?.let { Uri.parse(it) }
            )
            .build()

        firebaseUser
            .updateProfile(request)
            .await()
    }

    private suspend fun ensureUserDocument(firebaseUser: FirebaseUser): AppUser {
        val document = firestore
            .collection(USERS)
            .document(firebaseUser.uid)
            .get()
            .await()

        val existing = AppUser.fromDocument(document)

        if (existing != null) {
            return existing
        }

        val appUser = AppUser(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName
                ?: firebaseUser.email?.substringBefore("@")
                ?: "Usuario",
            email = firebaseUser.email.orEmpty(),
            photoUrl = firebaseUser.photoUrl?.toString().orEmpty(),
            createdAt = LocalDateTime.now()
        )

        firestore
            .collection(USERS)
            .document(firebaseUser.uid)
            .set(
                appUser.toProfileMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()

        return appUser
    }

    private suspend fun uploadProfilePhoto(
        uid: String,
        uri: Uri
    ): UploadedPhoto {
        val storagePath = "profile_images/$uid/profile_${System.currentTimeMillis()}.jpg"

        val reference = storage
            .reference
            .child(storagePath)

        reference
            .putFile(uri)
            .await()

        val downloadUrl = reference
            .downloadUrl
            .await()
            .toString()

        return UploadedPhoto(
            downloadUrl = downloadUrl,
            storagePath = storagePath
        )
    }

    private suspend fun saveMessagingToken(
        uid: String,
        token: String
    ) {
        val tokenHash = token.sha256()

        val tokenData = mapOf(
            "token" to token,
            "platform" to "android",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val tokenWrite = firestore
            .collection(USERS)
            .document(uid)
            .collection(TOKENS)
            .document(tokenHash)
            .set(tokenData, SetOptions.merge())

        val userWrite = firestore
            .collection(USERS)
            .document(uid)
            .set(
                mapOf(
                    "lastMessagingToken" to token,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

        Tasks
            .whenAll(tokenWrite, userWrite)
            .await()
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    private data class UploadedPhoto(
        val downloadUrl: String = "",
        val storagePath: String = ""
    )

    private companion object {
        const val USERS = "users"
        const val EVENTS = "events"
        const val TOKENS = "tokens"
    }
}