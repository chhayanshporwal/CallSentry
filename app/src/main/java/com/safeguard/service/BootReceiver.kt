package com.safeguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("SafeGuard", "Device booted - SafeGuard services will be active")
            // The CallScreeningService and SmsReceiver are automatically
            // registered via manifest, so they will be active after boot.
            // No additional action needed here.
        }
    }
}
