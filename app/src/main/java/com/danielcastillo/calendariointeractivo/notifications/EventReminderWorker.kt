package com.danielcastillo.calendariointeractivo.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "Recordatorio de evento"
        val body = inputData.getString(KEY_BODY) ?: "Tienes un evento en el calendario."
        NotificationHelper.showEventReminder(
            context = applicationContext,
            eventId = eventId,
            title = title,
            body = body
        )
        return Result.success()
    }

    companion object {
        const val KEY_EVENT_ID = "eventId"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
