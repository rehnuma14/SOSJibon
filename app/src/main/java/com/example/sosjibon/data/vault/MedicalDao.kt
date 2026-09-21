package com.example.sosjibon.data.vault

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {
    @Query("SELECT * FROM health_readings ORDER BY dateMillis ASC")
    fun readings(): Flow<List<HealthReading>>

    @Insert
    fun addReading(item: HealthReading)

    @Delete
    fun deleteReading(item: HealthReading)

    @Query("SELECT * FROM vault_documents ORDER BY dateMillis DESC")
    fun vault(): Flow<List<VaultDocument>>

    @Insert
    fun addVault(item: VaultDocument)

    @Delete
    fun deleteVault(item: VaultDocument)

    @Query("SELECT * FROM medications ORDER BY required DESC, name ASC")
    fun medications(): Flow<List<Medication>>

    @Insert
    fun addMedication(item: Medication)

    @Delete
    fun deleteMedication(item: Medication)

    @Query("SELECT * FROM donation_records ORDER BY timestamp DESC")
    fun donationRecords(): Flow<List<DonationRecord>>

    @Query("SELECT * FROM donation_records ORDER BY donationDate DESC")
    fun getDonationRecordsSync(): List<DonationRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDonationRecord(item: DonationRecord)

    @Delete
    fun deleteDonationRecord(item: DonationRecord)
}
