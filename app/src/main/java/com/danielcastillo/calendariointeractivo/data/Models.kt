package com.danielcastillo.calendariointeractivo.data

import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SpanishLocale: Locale = Locale.forLanguageTag("es-ES")

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val photoStoragePath: String = "",
    val messagingToken: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toProfileMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "photoStoragePath" to photoStoragePath
        )
    }

    companion object {
        fun fromDocument(document: DocumentSnapshot): AppUser? {
            if (!document.exists()) return null

            val createdAtDateTime = document.getTimestamp("createdAt")
                ?.toDate()
                ?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDateTime()
                ?: LocalDateTime.now()

            return AppUser(
                uid = document.getString("uid") ?: document.id,
                name = document.getString("name").orEmpty(),
                email = document.getString("email").orEmpty(),
                photoUrl = document.getString("photoUrl").orEmpty(),
                photoStoragePath = document.getString("photoStoragePath").orEmpty(),
                messagingToken = document.getString("lastMessagingToken").orEmpty(),
                createdAt = createdAtDateTime
            )
        }
    }
}

data class EventDraft(
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val colorHex: String
)

data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val colorHex: String = "#10B7A7",
    val creatorUid: String = "",
    val creatorName: String = "",
    val creatorPhotoUrl: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun occursOn(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    fun dateLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", SpanishLocale)

        return if (startDate == endDate) {
            startDate.format(formatter)
        } else {
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        }
    }

    fun createdLabel(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", SpanishLocale)

        return Instant
            .ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    fun endsInDaysFromToday(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate)
    }

    companion object {
        fun fromDocument(document: DocumentSnapshot): CalendarEvent? {
            if (!document.exists()) return null

            val startDate = document.getString("startDate")?.toLocalDateOrNull()
                ?: document.getLong("startDateMillis")?.toLocalDateFromMillis()
                ?: LocalDate.now()

            val endDate = document.getString("endDate")?.toLocalDateOrNull()
                ?: document.getLong("endDateMillis")?.toLocalDateFromMillis()
                ?: startDate

            val createdAtMillis = document.getLong("createdAtMillis")
                ?: document.getTimestamp("createdAt")?.toDate()?.time
                ?: System.currentTimeMillis()

            return CalendarEvent(
                id = document.id,
                title = document.getString("title").orEmpty(),
                description = document.getString("description").orEmpty(),
                startDate = startDate,
                endDate = endDate,
                colorHex = document.getString("colorHex") ?: "#10B7A7",
                creatorUid = document.getString("creatorUid").orEmpty(),
                creatorName = document.getString("creatorName").orEmpty(),
                creatorPhotoUrl = document.getString("creatorPhotoUrl").orEmpty(),
                createdAtMillis = createdAtMillis
            )
        }
    }
}

fun LocalDate.toIsoString(): String {
    return this.toString()
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching {
        LocalDate.parse(this)
    }.getOrNull()
}

private fun Long.toLocalDateFromMillis(): LocalDate {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}