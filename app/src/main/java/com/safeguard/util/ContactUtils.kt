package com.safeguard.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ContactUtils @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun getContactName(phoneNumber: String): String? =
            withContext(Dispatchers.IO) {
                if (phoneNumber.isBlank()) return@withContext null

                val uri =
                        Uri.withAppendedPath(
                                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                                Uri.encode(phoneNumber)
                        )

                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

                try {
                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor
                        ->
                        if (cursor.moveToFirst()) {
                            val nameIndex =
                                    cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                            if (nameIndex != -1) {
                                return@withContext cursor.getString(nameIndex)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return@withContext null
            }
}
