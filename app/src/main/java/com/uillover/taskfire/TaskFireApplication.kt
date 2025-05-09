package com.uillover.taskfire



import android.app.Application
import com.google.firebase.FirebaseApp

class TaskFireApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
