package com.danielcastillo.calendariointeractivo

import android.app.Application
import com.danielcastillo.calendariointeractivo.notifications.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class InteractiveCalendarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(true)

        FirebaseAnalytics.getInstance(this)
            .logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        NotificationHelper.ensureChannels(this)
    }
}