package com.example.sosjibon.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed. Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        pass: String,
        phone: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (_: Exception) {
                }

                try {
                    withTimeoutOrNull(15000) {
                        FirestoreManager().saveUserProfile(
                            uid = user.uid,
                            fullName = fullName,
                            email = email,
                            phone = phone
                        )
                    }
                } catch (_: Exception) {
                }

                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed. Please try again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                try {
                    val emailLower = (user.email ?: "").trim().lowercase()
                    val role = if (emailLower == "admin@gmail.com") "admin" else "user"
                    val userMap = hashMapOf(
                        "uid" to user.uid,
                        "fullName" to (user.displayName ?: "Google User"),
                        "email" to emailLower,
                        "phone" to (user.phoneNumber ?: ""),
                        "photoUrl" to (user.photoUrl?.toString() ?: ""),
                        "role" to role,
                        "isEmailVerified" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .set(userMap, SetOptions.merge())
                        .await()
                } catch (_: Exception) {}

                Result.success(user)
            } else {
                Result.failure(Exception("Google sign in failed."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
