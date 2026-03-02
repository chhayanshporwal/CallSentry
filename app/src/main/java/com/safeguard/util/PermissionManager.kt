package com.safeguard.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun hasCallScreeningRole(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
        return true // For older versions where this role concept is different/n/a
    }

    fun createRequestRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        }
        return null
    }

    fun hasRuntimePermissions(): Boolean {
        val permissions =
                listOf(
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.READ_CALL_LOG,
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.RECEIVE_SMS
                )

        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS
        )
    }
}
