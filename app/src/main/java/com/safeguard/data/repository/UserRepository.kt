package com.safeguard.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safeguard.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")
    private val emailLookup = firestore.collection("emailLookup")
    private val phoneLookup = firestore.collection("phoneLookup")

    companion object {
        private const val TAG = "UserRepository"
    }

    // ── Profile CRUD ──────────────────────────────────────────────

    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(userProfile.uid).set(userProfile, SetOptions.merge()).await()

            // Claim credentials in lookup collections
            if (!userProfile.email.isNullOrBlank()) {
                claimEmail(userProfile.email, userProfile.uid)
            }
            if (userProfile.phoneNumber.isNotBlank()) {
                claimPhone(userProfile.phoneNumber, userProfile.uid)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user profile", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserProfile(uid: String): Result<Unit> {
        return try {
            usersCollection.document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user profile", e)
            Result.failure(e)
        }
    }

    // ── Uniqueness checks via lookup collections ──────────────────

    /**
     * Checks if an email is available. Returns true if the email is not claimed
     * by any other user (or not claimed at all).
     */
    suspend fun isEmailUnique(email: String, currentUid: String): Result<Boolean> {
        return try {
            val docKey = email.lowercase().trim()
            val doc = emailLookup.document(docKey).get().await()
            if (!doc.exists()) {
                Result.success(true)
            } else {
                val ownerUid = doc.getString("uid")
                Result.success(ownerUid == currentUid)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email uniqueness check failed", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if a phone number is available. Returns true if the phone is not
     * claimed by any other user (or not claimed at all).
     */
    suspend fun isPhoneUnique(phone: String, currentUid: String): Result<Boolean> {
        return try {
            val docKey = phone.trim()
            val doc = phoneLookup.document(docKey).get().await()
            if (!doc.exists()) {
                Result.success(true)
            } else {
                val ownerUid = doc.getString("uid")
                Result.success(ownerUid == currentUid)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Phone uniqueness check failed", e)
            Result.failure(e)
        }
    }

    // ── Claim / Release credentials ───────────────────────────────

    suspend fun claimEmail(email: String, uid: String) {
        try {
            val docKey = email.lowercase().trim()
            emailLookup.document(docKey).set(hashMapOf("uid" to uid)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to claim email in lookup", e)
        }
    }

    suspend fun releaseEmail(email: String, uid: String) {
        try {
            val docKey = email.lowercase().trim()
            val doc = emailLookup.document(docKey).get().await()
            if (doc.exists() && doc.getString("uid") == uid) {
                emailLookup.document(docKey).delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release email from lookup", e)
        }
    }

    suspend fun claimPhone(phone: String, uid: String) {
        try {
            val docKey = phone.trim()
            phoneLookup.document(docKey).set(hashMapOf("uid" to uid)).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to claim phone in lookup", e)
        }
    }

    suspend fun releasePhone(phone: String, uid: String) {
        try {
            val docKey = phone.trim()
            val doc = phoneLookup.document(docKey).get().await()
            if (doc.exists() && doc.getString("uid") == uid) {
                phoneLookup.document(docKey).delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release phone from lookup", e)
        }
    }
}
