package com.safeguard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safeguard.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(userProfile.uid).set(userProfile, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserProfile(uid: String): Result<Unit> {
        return try {
            usersCollection.document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEmailUnique(email: String, currentUid: String): Result<Boolean> {
        return try {
            val snapshot = usersCollection.whereEqualTo("email", email).get().await()
            val isUnique = snapshot.documents.all { it.id == currentUid }
            Result.success(isUnique)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isPhoneUnique(phone: String, currentUid: String): Result<Boolean> {
        return try {
            val snapshot = usersCollection.whereEqualTo("phoneNumber", phone).get().await()
            val isUnique = snapshot.documents.all { it.id == currentUid }
            Result.success(isUnique)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
