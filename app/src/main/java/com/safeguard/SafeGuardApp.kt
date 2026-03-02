package com.safeguard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SafeGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val intent = android.content.Intent(applicationContext, com.safeguard.presentation.CrashActivity::class.java).apply {
                    putExtra("error_trace", android.util.Log.getStackTraceString(throwable))
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            } catch (e: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
