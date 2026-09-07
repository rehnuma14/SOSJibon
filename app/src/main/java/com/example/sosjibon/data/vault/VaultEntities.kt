package com.example.sosjibon.data.vault

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_readings")
data class HealthReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "",
    val value: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_documents")
data class VaultDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val disease: String = "",
    val uri: String = "",
    val displayName: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val dose: String = "",
    val timing: String = "",
    val required: Boolean = false,
    val note: String = ""
)
