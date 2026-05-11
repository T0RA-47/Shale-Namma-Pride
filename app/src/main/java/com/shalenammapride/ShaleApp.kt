package com.shalenammapride

import android.app.Application
import com.google.firebase.FirebaseApp

class ShaleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
