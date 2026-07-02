package com.danielcastillo.calendariointeractivo.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.danielcastillo.calendariointeractivo.data.CalendarEvent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private val notificationTime: LocalTime = LocalTime.of(9, 0)

    fun scheduleForEvents(context: Context, events: List<CalendarEvent>) {
        events.forEach { event -> scheduleEvent(context.applicationContext, event) }
    }

    private fun scheduleEvent(context: Context, event: CalendarEvent) {
        val fifteenDaysBefore = event.startDate.minusDays(15).atTime(notificationTime)
        scheduleReminder(
            context = context,
            uniqueName = "event_${event.id.safeName()}_minus_15",
            eventId = "${event.id}_minus_15",
            runAt = fifteenDaysBefore,
            title = "Faltan 15 dias: ${event.title}",
            body = "Evento creado por ${event.creatorName}. Fecha: ${event.dateLabel()}."
        )

        var cursor = event.startDate
        var count = 0
        while (!cursor.isAfter(event.endDate) && count < 366) {
            val runAt = eventDayReminderTime(cursor)
            scheduleReminder(
                context = context,
                uniqueName = "event_${event.id.safeName()}_${cursor}",
                eventId = "${event.id}_$cursor",
                runAt = runAt,
                title = "Hoy: ${event.title}",
                body = "Publicado por ${event.creatorName}. ${event.description.ifBlank { "Consulta el calendario para mas detalles." }}"
            )
            cursor = cursor.plusDays(1)
            count += 1
        }
    }

    private fun scheduleReminder(
        context: Context,
        uniqueName: String,
        eventId: String,
        runAt: LocalDateTime,
        title: String,
        body: String
    ) {
        val now = LocalDateTime.now()
        if (runAt.isBefore(now.minusMinutes(1))) return

        val delay = Duration.between(now, runAt.coerceAtLeast(now.plusSeconds(10))).toMillis()
        val data = Data.Builder()
            .putString(EventReminderWorker.KEY_EVENT_ID, eventId)
            .putString(EventReminderWorker.KEY_TITLE, title)
            .putString(EventReminderWorker.KEY_BODY, body)
            .build()

        val request = OneTimeWorkRequest.Builder(EventReminderWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("calendar_event_reminder")
            .addTag(eventId.safeName())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun eventDayReminderTime(date: LocalDate): LocalDateTime {
        val planned = date.atTime(notificationTime)
        val now = LocalDateTime.now()
        return if (date == LocalDate.now() && planned.isBefore(now)) {
            now.plusMinutes(1)
        } else {
            planned
        }
    }

    private fun LocalDateTime.coerceAtLeast(minimum: LocalDateTime): LocalDateTime {
        return if (isBefore(minimum)) minimum else this
    }

    private fun String.safeName(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")
}
