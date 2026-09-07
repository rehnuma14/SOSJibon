package com.example.sosjibon.ui.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sosjibon.data.firebase.FirestoreManager
import com.example.sosjibon.data.vault.HealthReading
import com.example.sosjibon.data.vault.Medication
import com.example.sosjibon.data.vault.VaultDatabase
import com.example.sosjibon.data.vault.VaultDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = VaultDatabase.get(app).dao()
    private val firestore = FirestoreManager()

    val readings = dao.readings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val vault = dao.vault().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val medications = dao.medications().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addReading(type: String, value: String) = viewModelScope.launch(Dispatchers.IO) {
        val reading = HealthReading(type = type, value = value)
        dao.addReading(reading)
        firestore.addHealthReading(reading)
    }

    fun deleteReading(x: HealthReading) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteReading(x)
    }

    fun addVault(disease: String, uri: String, name: String) = viewModelScope.launch(Dispatchers.IO) {
        val doc = VaultDocument(disease = disease, uri = uri, displayName = name)
        dao.addVault(doc)
        firestore.addVaultDocument(doc)
    }

    fun deleteVault(x: VaultDocument) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteVault(x)
    }

    fun addMedication(name: String, dose: String, timing: String, required: Boolean, note: String) = viewModelScope.launch(Dispatchers.IO) {
        val med = Medication(
            name = name,
            dose = dose,
            timing = timing,
            required = required,
            note = note
        )
        dao.addMedication(med)
        firestore.addMedication(med)
    }

    fun deleteMedication(x: Medication) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteMedication(x)
    }
}
