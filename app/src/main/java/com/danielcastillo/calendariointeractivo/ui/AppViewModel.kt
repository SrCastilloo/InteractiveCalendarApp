package com.danielcastillo.calendariointeractivo.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danielcastillo.calendariointeractivo.data.AppUser
import com.danielcastillo.calendariointeractivo.data.CalendarEvent
import com.danielcastillo.calendariointeractivo.data.EventDraft
import com.danielcastillo.calendariointeractivo.data.FirebaseCalendarRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

data class UiMessage(
    val text: String,
    val isError: Boolean
)

data class AppUiState(
    val isLoading: Boolean = true,
    val operationInProgress: Boolean = false,
    val user: AppUser? = null,
    val events: List<CalendarEvent> = emptyList(),
    val message: UiMessage? = null,
    val eventCreatedVisualToken: Int = 0
) {
    val isAuthenticated: Boolean get() = user != null
}

class AppViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FirebaseCalendarRepository(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var eventsJob: Job? = null

    init {
        observeAuth()
    }

    fun register(
        name: String,
        email: String,
        password: String,
        photoUri:    Uri?
    ) {
        launchOperation(success = "Cuenta creada correctamente. ¡Bienvenido!") {
            val user = repository.register(name, email, password, photoUri)
            _uiState.update { it.copy(user = user) }
            startEventStream()
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        launchOperation(success = "Sesión iniciada correctamente.") {
            val user = repository.login(email, password)
            _uiState.update { it.copy(user = user) }
            startEventStream()
        }
    }

    fun resetPassword(email: String) {
        launchOperation(success = "Te hemos enviado un correo para recuperar la contraseña.") {
            repository.sendPasswordReset(email)
        }
    }

    fun createEvent(draft: EventDraft) {
        _uiState.update {
            it.copy(
                operationInProgress = true,
                message = null
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.createEvent(draft)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        operationInProgress = false,
                        message = UiMessage("Evento creado correctamente.", false),
                        eventCreatedVisualToken = it.eventCreatedVisualToken + 1
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        operationInProgress = false,
                        message = UiMessage(error.humanMessage(), true)
                    )
                }
            }
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        launchOperation(success = "Evento eliminado correctamente.") {
            repository.deleteEvent(event.id)
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        photoUri: Uri?,
        newPassword: String?
    ) {
        launchOperation(success = "Perfil actualizado correctamente.") {
            val user = repository.updateProfile(name, email, photoUri, newPassword)
            _uiState.update { it.copy(user = user) }
        }
    }

    fun refreshMessagingToken() {
        viewModelScope.launch {
            runCatching {
                repository.refreshMessagingTokenForCurrentUser()
            }
        }
    }

    fun logout() {
        eventsJob?.cancel()
        repository.logout()
        _uiState.value = AppUiState(isLoading = false)
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    private fun observeAuth() {
        viewModelScope.launch {
            repository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser == null) {
                    eventsJob?.cancel()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = null,
                            events = emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }

                    runCatching {
                        repository.fetchCurrentUserProfile()
                    }.onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = profile,
                                message = null
                            )
                        }

                        refreshMessagingToken()
                        startEventStream()
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = UiMessage(error.humanMessage(), true)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startEventStream() {
        eventsJob?.cancel()

        eventsJob = viewModelScope.launch {
            repository.observeEvents()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            message = UiMessage(error.humanMessage(), true)
                        )
                    }
                }
                .collect { events ->
                    _uiState.update {
                        it.copy(events = events)
                    }
                }
        }
    }

    private fun launchOperation(
        success: String,
        block: suspend () -> Unit
    ) {
        _uiState.update {
            it.copy(
                operationInProgress = true,
                message = null
            )
        }

        viewModelScope.launch {
            runCatching {
                block()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        operationInProgress = false,
                        message = UiMessage(success, false)
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        operationInProgress = false,
                        message = UiMessage(error.humanMessage(), true)
                    )
                }
            }
        }
    }
}

class AppViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(application) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

private fun Throwable.humanMessage(): String {
    return when (this) {
        is FirebaseAuthRecentLoginRequiredException -> {
            "Por seguridad, vuelve a iniciar sesión antes de cambiar tu correo o contraseña."
        }

        is FirebaseNetworkException -> {
            "No se ha podido conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo."
        }

        is FirebaseAuthException -> {
            firebaseAuthFriendlyMessage()
        }

        is FirebaseFirestoreException -> {
            firestoreFriendlyMessage()
        }

        is StorageException -> {
            storageFriendlyMessage()
        }

        is UnknownHostException -> {
            "No hay conexión a internet. Comprueba tu red e inténtalo otra vez."
        }

        else -> {
            val cleanMessage = message.orEmpty()

            when {
                cleanMessage.contains("network", ignoreCase = true) ||
                        cleanMessage.contains("internet", ignoreCase = true) -> {
                    "Parece que no hay conexión. Revisa internet e inténtalo otra vez."
                }

                cleanMessage.contains("permission", ignoreCase = true) ||
                        cleanMessage.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                    "No tienes permisos para realizar esta acción."
                }

                cleanMessage.contains("Solo puede eliminar", ignoreCase = true) -> {
                    "Solo puede eliminar este evento la persona que lo creó."
                }

                cleanMessage.contains("evento necesita un título", ignoreCase = true) -> {
                    "Ponle un título al evento antes de publicarlo."
                }

                cleanMessage.isNotBlank() -> {
                    "No se ha podido completar la acción. Revisa los datos e inténtalo de nuevo."
                }

                else -> {
                    "Algo no ha ido bien. Inténtalo de nuevo en unos segundos."
                }
            }
        }
    }
}

private fun FirebaseAuthException.firebaseAuthFriendlyMessage(): String {
    return when (errorCode) {
        "ERROR_INVALID_EMAIL" -> {
            "El correo no tiene un formato válido. Revísalo e inténtalo de nuevo."
        }

        "ERROR_USER_NOT_FOUND" -> {
            "No existe ninguna cuenta con ese correo. Puedes registrarte para crear una nueva."
        }

        "ERROR_WRONG_PASSWORD" -> {
            "La contraseña no es correcta. Revisa que esté bien escrita."
        }

        "ERROR_INVALID_CREDENTIAL" -> {
            "El correo o la contraseña no son correctos. Revísalos e inténtalo de nuevo."
        }

        "ERROR_EMAIL_ALREADY_IN_USE" -> {
            "Ya existe una cuenta con ese correo. Prueba a iniciar sesión."
        }

        "ERROR_WEAK_PASSWORD" -> {
            "La contraseña es demasiado débil. Usa al menos 6 caracteres."
        }

        "ERROR_TOO_MANY_REQUESTS" -> {
            "Has hecho demasiados intentos seguidos. Espera un poco y vuelve a intentarlo."
        }

        "ERROR_USER_DISABLED" -> {
            "Esta cuenta está desactivada. Contacta con el administrador."
        }

        "ERROR_OPERATION_NOT_ALLOWED" -> {
            "El inicio de sesión con correo y contraseña no está activado en Firebase."
        }

        "ERROR_REQUIRES_RECENT_LOGIN" -> {
            "Por seguridad, vuelve a iniciar sesión antes de hacer este cambio."
        }

        "ERROR_NETWORK_REQUEST_FAILED" -> {
            "No se ha podido conectar con Firebase. Revisa tu conexión a internet."
        }

        else -> {
            "No se ha podido acceder con esos datos. Revísalos e inténtalo otra vez."
        }
    }
}

private fun FirebaseFirestoreException.firestoreFriendlyMessage(): String {
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
            "No tienes permisos para realizar esta acción."
        }

        FirebaseFirestoreException.Code.UNAVAILABLE -> {
            "El servidor no está disponible ahora mismo. Inténtalo de nuevo en unos segundos."
        }

        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
            "La conexión está tardando demasiado. Revisa internet e inténtalo otra vez."
        }

        FirebaseFirestoreException.Code.NOT_FOUND -> {
            "No se ha encontrado la información solicitada."
        }

        else -> {
            "No se ha podido sincronizar la información. Inténtalo de nuevo."
        }
    }
}

private fun StorageException.storageFriendlyMessage(): String {
    return when (errorCode) {
        StorageException.ERROR_NOT_AUTHENTICATED -> {
            "Debes iniciar sesión para subir una foto."
        }

        StorageException.ERROR_NOT_AUTHORIZED -> {
            "No tienes permisos para subir esta imagen."
        }

        StorageException.ERROR_QUOTA_EXCEEDED -> {
            "No se ha podido subir la imagen porque se ha superado el límite de almacenamiento."
        }

        StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> {
            "La subida de la foto está tardando demasiado. Revisa tu conexión e inténtalo otra vez."
        }

        else -> {
            "No se ha podido subir la foto. Prueba con otra imagen o inténtalo más tarde."
        }
    }
}