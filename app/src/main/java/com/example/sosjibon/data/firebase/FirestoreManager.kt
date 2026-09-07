package com.example.sosjibon.data.firebase

import com.example.sosjibon.data.vault.HealthReading
import com.example.sosjibon.data.vault.Medication
import com.example.sosjibon.data.vault.VaultDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Save extended user profile
    suspend fun saveUserProfile(
        uid: String,
        fullName: String,
        email: String,
        phone: String
    ): Result<Unit> {
        return try {
            val userMap = hashMapOf(
                "uid" to uid,
                "fullName" to fullName,
                "email" to email,
                "phone" to phone,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Vault Documents Sync
    fun getVaultDocuments(): Flow<List<VaultDocument>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(uid)
            .collection("vault_documents")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(VaultDocument::class.java)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addVaultDocument(doc: VaultDocument): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
            val ref = db.collection("users").document(uid).collection("vault_documents").document()
            val item = doc.copy(id = ref.id.hashCode().toLong())
            ref.set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Health Readings Sync
    fun getHealthReadings(): Flow<List<HealthReading>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(uid)
            .collection("health_readings")
            .orderBy("dateMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(HealthReading::class.java)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addHealthReading(reading: HealthReading): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
            val ref = db.collection("users").document(uid).collection("health_readings").document()
            val item = reading.copy(id = ref.id.hashCode().toLong())
            ref.set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Medications Sync
    fun getMedications(): Flow<List<Medication>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(uid)
            .collection("medications")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Medication::class.java)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addMedication(med: Medication): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
            val ref = db.collection("users").document(uid).collection("medications").document()
            val item = med.copy(id = ref.id.hashCode().toLong())
            ref.set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
