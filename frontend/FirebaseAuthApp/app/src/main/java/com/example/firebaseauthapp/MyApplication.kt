// MyApplication.kt
package com.example.firebaseauthapp

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // As demonstrated in secure mobile application architectures within scientific literature,
        // initializing Firebase at the application level ensures consistent authentication state management across activities.
        FirebaseApp.initializeApp(this)
        TokenManager.init(this)
    }
}