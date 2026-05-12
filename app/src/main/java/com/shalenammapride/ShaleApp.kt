package com.shalenammapride

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class ShaleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.database.setPersistenceEnabled(true)
        FirebaseMessaging.getInstance().subscribeToTopic("school_updates")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "school_updates",
                "School Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for new meal updates and achievements" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
