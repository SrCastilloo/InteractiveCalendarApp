package com.danielcastillo.calendariointeractivo.notifications

import com.danielcastillo.calendariointeractivo.data.FirebaseCalendarRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            runCatching {
                FirebaseCalendarRepository(applicationContext)
                    .saveMessagingTokenForCurrentUser(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val eventId = message.data["eventId"] ?: message.messageId ?: "fcm_calendar_message"
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Calendario interactivo"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Hay una nueva actualizacion en el calendario."

        NotificationHelper.showEventReminder(
            context = applicationContext,
            eventId = eventId,
            title = title,
            body = body
        )
    }
}
