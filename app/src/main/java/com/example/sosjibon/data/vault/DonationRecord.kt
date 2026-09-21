package com.example.sosjibon.data.vault

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "donation_records")
data class DonationRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val donationDate: String = "",
    val locationOrHospital: String = "Local Blood Bank",
    val notes: String = "Blood donation",
    val timestamp: Long = System.currentTimeMillis()
)
