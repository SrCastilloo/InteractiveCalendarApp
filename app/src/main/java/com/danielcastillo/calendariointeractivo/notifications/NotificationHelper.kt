package com.danielcastillo.calendariointeractivo.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.danielcastillo.calendariointeractivo.MainActivity
import com.danielcastillo.calendariointeractivo.R
import kotlin.math.absoluteValue

object NotificationHelper {
    const val REMINDER_CHANNEL_ID = "calendar_event_reminders"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Recordatorios de eventos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos 15 dias antes y durante cada evento del calendario."
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showEventReminder(
        context: Context,
        eventId: String,
        title: String,
        body: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannels(context)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode().absoluteValue,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(eventId.hashCode().absoluteValue, notification)
    }
}
