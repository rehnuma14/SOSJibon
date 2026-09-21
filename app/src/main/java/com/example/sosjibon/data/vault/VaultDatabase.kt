package com.example.sosjibon.data.vault

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HealthReading::class, VaultDocument::class, Medication::class, DonationRecord::class],
    version = 2,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun dao(): MedicalDao

    companion object {
        @Volatile
        private var instance: VaultDatabase? = null

        fun get(context: Context): VaultDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                "sosjibon_vault_history.db"
            )
            .fallbackToDestructiveMigration()
            .build().also { instance = it }
        }
    }
}
