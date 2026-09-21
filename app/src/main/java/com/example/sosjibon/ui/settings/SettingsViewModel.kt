package com.example.sosjibon.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sosjibon.data.email.EmailService
import com.example.sosjibon.data.firebase.FirebaseAuthManager
import com.example.sosjibon.data.firebase.FirestoreManager
import com.example.sosjibon.data.vault.DonationRecord
import com.example.sosjibon.data.vault.VaultDatabase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

data class EmergencyContactItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val bloodGroup: String = "O+",
    val gender: String = "Male",
    val relation: String = "Family"
)

data class UserProfileDetails(
    val fullName: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false,
    val phone: String = "",
    val bloodGroup: String = "",
    val gender: String = "Male",
    val lastDonationDate: String = "",
    val dob: String = "",
    val country: String = "",
    val city: String = "",
    val imageUri: String = "",
    val shortDescription: String = ""
)

data class DeveloperItem(
    val name: String,
    val role: String,
    val designation: String,
    val contribution: String,
    val email: String,
    val linkedin: String,
    val initial: String
)

data class SettingsState(
    val profile: UserProfileDetails = UserProfileDetails(),
    val emergencyContacts: List<EmergencyContactItem> = emptyList(),
    val developers: List<DeveloperItem> = listOf(
        DeveloperItem(
            name = "Shafiul Islam",
            role = "Lead Developer",
            designation = "Lead Systems & Android Engineer",
            contribution = "Architecture, App Navigation, Emergency SOS Engine, and Compose UI Framework.",
            email = "shafiul@sosjibon.org",
            linkedin = "linkedin.com/in/shafiul-islam",
            initial = "SI"
        ),
        DeveloperItem(
            name = "Mahmudul Hasan",
            role = "Backend Lead",
            designation = "Cloud & Security Infrastructure Lead",
            contribution = "Firebase Authentication, Cloud Firestore Realtime Sync, and Encrypted Storage.",
            email = "mahmudul@sosjibon.org",
            linkedin = "linkedin.com/in/mahmudul-hasan",
            initial = "MH"
        ),
        DeveloperItem(
            name = "Nusrat Jahan",
            role = "UI/UX Specialist",
            designation = "Mobile Product & Experience Designer",
            contribution = "Healthcare Vault UI, Vitals Trend Canvas Visualization, and Design System.",
            email = "nusrat@sosjibon.org",
            linkedin = "linkedin.com/in/nusrat-jahan",
            initial = "NJ"
        ),
        DeveloperItem(
            name = "Tanvir Ahmed",
            role = "QA & Data Lead",
            designation = "Quality Assurance & Health Systems Lead",
            contribution = "Emergency GPS Location Services, First Aid E-Library, and End-to-End Testing.",
            email = "tanvir@sosjibon.org",
            linkedin = "linkedin.com/in/tanvir-ahmed",
            initial = "TA"
        )
    ),
    val isLoggedIn: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val emergencyAlertsEnabled: Boolean = true,
    val language: String = "English",
    val themeMode: String = "system",
    val isDarkTheme: Boolean = false,
    val isAdminRole: Boolean = false,
    val generatedCode: String? = null,
    val message: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = VaultDatabase.get(app).dao()
    private val authManager = FirebaseAuthManager()
    private val emailService = EmailService()
    private val firestore = FirestoreManager()
    private val prefs = app.getSharedPreferences("sosjibon_profile_settings", Context.MODE_PRIVATE)

    private var userSnapshotRegistration: ListenerRegistration? = null
    private var contactsSnapshotRegistration: ListenerRegistration? = null

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    val donationRecords: StateFlow<List<DonationRecord>> = dao.donationRecords().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        loadSettings()

        try {
            FirebaseAuth.getInstance().addAuthStateListener {
                loadSettings()
            }
            prefs.registerOnSharedPreferenceChangeListener { _, _ ->
                loadSettings()
            }

            val currentUser = authManager.currentUser
            if (currentUser != null) {
                FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val verified = snapshot.getBoolean("isEmailVerified") == true
                            if (verified != _state.value.profile.isEmailVerified) {
                                _state.value = _state.value.copy(
                                    profile = _state.value.profile.copy(isEmailVerified = verified)
                                )
                                val emailLower = (currentUser.email ?: "").trim().lowercase()
                                if (emailLower.isNotBlank()) {
                                    prefs.edit().putBoolean("user_email_verified_$emailLower", verified).apply()
                                }
                            }
                        }
                    }
            }
        } catch (_: Exception) {}

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.getDonationRecords().collect { records ->
                    records.forEach { r -> dao.addDonationRecord(r) }
                }
            } catch (_: Exception) {}
        }
    }

    private fun saveContactsToPrefs(contacts: List<EmergencyContactItem>) {
        try {
            val array = JSONArray()
            contacts.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("bloodGroup", c.bloodGroup)
                    put("gender", c.gender)
                    put("relation", c.relation)
                }
                array.put(obj)
            }
            prefs.edit().putString("saved_emergency_contacts", array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadContactsFromPrefs(): List<EmergencyContactItem> {
        val list = mutableListOf<EmergencyContactItem>()
        try {
            val jsonStr = prefs.getString("saved_emergency_contacts", "") ?: ""
            if (jsonStr.isNotBlank()) {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        EmergencyContactItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", ""),
                            phone = obj.optString("phone", ""),
                            bloodGroup = obj.optString("bloodGroup", "O+"),
                            gender = obj.optString("gender", "Male"),
                            relation = obj.optString("relation", "Family")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun attachUserFirestoreListener(uid: String) {
        try {
            userSnapshotRegistration?.remove()
            userSnapshotRegistration = FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val currentProf = _state.value.profile
                        val name = snapshot.getString("fullName")?.ifBlank { null } ?: prefs.getString("user_name", "")?.ifBlank { null } ?: currentProf.fullName
                        val email = snapshot.getString("email")?.ifBlank { null } ?: prefs.getString("user_email", "")?.ifBlank { null } ?: currentProf.email
                        val phone = snapshot.getString("phone")?.ifBlank { null } ?: prefs.getString("user_phone", "")?.ifBlank { null } ?: currentProf.phone
                        val bg = snapshot.getString("bloodGroup")?.ifBlank { null } ?: prefs.getString("user_blood_group", "")?.ifBlank { null } ?: currentProf.bloodGroup
                        val gender = snapshot.getString("gender")?.ifBlank { null } ?: prefs.getString("user_gender", "")?.ifBlank { null } ?: currentProf.gender
                        val lastDonation = snapshot.getString("lastDonationDate")?.ifBlank { null } ?: prefs.getString("user_last_donation_date", "")?.ifBlank { null } ?: currentProf.lastDonationDate
                        val dob = snapshot.getString("dob")?.ifBlank { null } ?: prefs.getString("user_dob", "")?.ifBlank { null } ?: currentProf.dob
                        val country = snapshot.getString("country")?.ifBlank { null } ?: prefs.getString("user_country", "")?.ifBlank { null } ?: currentProf.country
                        val city = snapshot.getString("city")?.ifBlank { null } ?: prefs.getString("user_city", "")?.ifBlank { null } ?: currentProf.city
                        val desc = snapshot.getString("shortDescription")?.ifBlank { null } ?: prefs.getString("user_desc", "")?.ifBlank { null } ?: currentProf.shortDescription
                        val imgUri = snapshot.getString("imageUri")?.ifBlank { null } ?: prefs.getString("user_img_uri", "")?.ifBlank { null } ?: currentProf.imageUri
                        val verified = snapshot.getBoolean("isEmailVerified") == true || currentProf.isEmailVerified

                        val updatedProfile = UserProfileDetails(
                            fullName = name,
                            email = email,
                            isEmailVerified = verified,
                            phone = phone,
                            bloodGroup = bg,
                            gender = gender,
                            lastDonationDate = lastDonation,
                            dob = dob,
                            country = country,
                            city = city,
                            imageUri = imgUri,
                            shortDescription = desc
                        )

                        _state.value = _state.value.copy(profile = updatedProfile)

                        val emailLower = email.trim().lowercase()
                        if (emailLower.isNotBlank()) {
                            prefs.edit().apply {
                                if (name.isNotBlank()) putString("user_name", name)
                                if (emailLower.isNotBlank()) putString("user_email", emailLower)
                                if (phone.isNotBlank()) putString("user_phone", phone)
                                if (bg.isNotBlank()) putString("user_blood_group", bg)
                                if (gender.isNotBlank()) putString("user_gender", gender)
                                if (lastDonation.isNotBlank()) putString("user_last_donation_date", lastDonation)
                                if (dob.isNotBlank()) putString("user_dob", dob)
                                if (country.isNotBlank()) putString("user_country", country)
                                if (city.isNotBlank()) putString("user_city", city)
                                if (desc.isNotBlank()) putString("user_desc", desc)
                                if (imgUri.isNotBlank()) putString("user_img_uri", imgUri)
                                putBoolean("user_email_verified", verified)
                                putBoolean("user_email_verified_$emailLower", verified)
                                apply()
                            }
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    private fun attachUserContactsListener(uid: String) {
        try {
            contactsSnapshotRegistration?.remove()
            contactsSnapshotRegistration = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("emergency_contacts")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val contacts = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(EmergencyContactItem::class.java)?.copy(id = doc.id)
                        }
                        _state.value = _state.value.copy(emergencyContacts = contacts)
                        saveContactsToPrefs(contacts)
                    }
                }
        } catch (_: Exception) {}
    }

    fun loadSettings(forceReload: Boolean = false) {
        val user = authManager.currentUser
        val savedName = prefs.getString("user_name", "") ?: ""
        val savedEmail = prefs.getString("user_email", "") ?: ""

        if (user == null) {
            userSnapshotRegistration?.remove()
            userSnapshotRegistration = null
            contactsSnapshotRegistration?.remove()
            contactsSnapshotRegistration = null

            val isGuest = savedName == "Guest Member"
            val profile = if (isGuest) {
                UserProfileDetails(fullName = "Guest Member", shortDescription = "Guest Session")
            } else {
                UserProfileDetails()
            }

            val contacts = loadContactsFromPrefs()

            _state.value = _state.value.copy(
                profile = profile,
                emergencyContacts = contacts,
                isLoggedIn = false,
                isAdminRole = false
            )
            return
        }

        attachUserFirestoreListener(user.uid)
        attachUserContactsListener(user.uid)

        val name = savedName.ifBlank { user.displayName ?: "" }
        val email = savedEmail.ifBlank { user.email ?: "" }
        val emailLower = email.trim().lowercase()
        val isVerified = user.isEmailVerified || (emailLower.isNotBlank() && prefs.getBoolean("user_email_verified_$emailLower", false))
        val phone = prefs.getString("user_phone", "") ?: ""
        val bg = prefs.getString("user_blood_group", "") ?: ""
        val gender = prefs.getString("user_gender", "Male") ?: "Male"
        val lastDonation = prefs.getString("user_last_donation_date", "") ?: ""
        val dob = prefs.getString("user_dob", "") ?: ""
        val country = prefs.getString("user_country", "") ?: ""
        val city = prefs.getString("user_city", "") ?: ""
        val imgUri = prefs.getString("user_img_uri", "") ?: ""
        val desc = prefs.getString("user_desc", "") ?: ""

        val notifs = prefs.getBoolean("notifications_enabled", true)
        val alerts = prefs.getBoolean("emergency_alerts_enabled", true)
        val lang = prefs.getString("language", "English") ?: "English"
        val themeMode = prefs.getString("theme_mode", "system") ?: "system"
        val dark = themeMode == "dark"

        val isAdmin = emailLower == "admin@gmail.com"

        val contacts = loadContactsFromPrefs()

        val profile = UserProfileDetails(
            fullName = name,
            email = email,
            isEmailVerified = isVerified,
            phone = phone,
            bloodGroup = bg,
            gender = gender,
            lastDonationDate = lastDonation,
            dob = dob,
            country = country,
            city = city,
            imageUri = imgUri,
            shortDescription = desc
        )

        _state.value = _state.value.copy(
            profile = profile,
            emergencyContacts = if (_state.value.emergencyContacts.isNotEmpty()) _state.value.emergencyContacts else contacts,
            isLoggedIn = true,
            notificationsEnabled = notifs,
            emergencyAlertsEnabled = alerts,
            language = lang,
            themeMode = themeMode,
            isDarkTheme = dark,
            isAdminRole = isAdmin
        )
    }

    fun sendEmailVerificationCode(onResult: (Boolean, String) -> Unit) {
        val currentEmail = _state.value.profile.email
        updateUserEmailAndSendVerification(currentEmail, onResult)
    }

    fun sendRealVerificationEmail(onResult: (Boolean, String) -> Unit) {
        val currentEmail = _state.value.profile.email
        updateUserEmailAndSendVerification(currentEmail, onResult)
    }

    fun updateUserEmailAndSendVerification(targetEmail: String, onResult: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        viewModelScope.launch {
            try {
                val activeEmail = targetEmail.trim().ifBlank { user?.email ?: "" }
                if (activeEmail.isBlank()) {
                    onResult(false, "Please enter a valid email address.")
                    return@launch
                }

                val code = (100000..999999).random().toString()
                _state.value = _state.value.copy(generatedCode = code)

                val emailResult = withTimeoutOrNull(20000L) {
                    emailService.sendOtpCodeEmail(activeEmail, code)
                } ?: Result.failure(Exception("Connection timed out (20s). Please check network connection."))

                if (user != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val map = hashMapOf(
                                "email" to activeEmail,
                                "verificationCode" to code,
                                "codeTimestamp" to System.currentTimeMillis()
                            )
                            FirebaseFirestore.getInstance().collection("users").document(user.uid).set(map, SetOptions.merge())
                        } catch (_: Exception) {}
                    }
                }

                if (emailResult.isSuccess) {
                    val text = emailResult.getOrNull() ?: "Verification code email sent to $activeEmail!"
                    _state.value = _state.value.copy(message = text)
                    onResult(true, text)
                } else {
                    val err = emailResult.exceptionOrNull()?.message ?: "Failed to send email."
                    _state.value = _state.value.copy(message = err)
                    onResult(false, err)
                }
            } catch (e: Exception) {
                val msg = "Error: ${e.message}"
                _state.value = _state.value.copy(message = msg)
                onResult(false, msg)
            }
        }
    }

    fun markEmailVerifiedDirectly(onResult: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val activeEmail = (user?.email ?: _state.value.profile.email).trim().lowercase()

        if (activeEmail.isNotBlank()) {
            prefs.edit().putBoolean("user_email_verified_$activeEmail", true).apply()
        }

        _state.value = _state.value.copy(
            profile = _state.value.profile.copy(isEmailVerified = true),
            message = "Email address verified successfully! ✓"
        )

        if (user != null) {
            viewModelScope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .update("isEmailVerified", true).await()
                } catch (_: Exception) {}
            }
        }
        onResult(true, "Email address verified successfully! ✓")
    }

    fun checkEmailVerificationLink(onResult: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            markEmailVerifiedDirectly(onResult)
            return
        }

        viewModelScope.launch {
            try {
                user.reload().await()
                val activeEmail = (user.email ?: "").trim().lowercase()
                if (user.isEmailVerified) {
                    if (activeEmail.isNotBlank()) {
                        prefs.edit().putBoolean("user_email_verified_$activeEmail", true).apply()
                    }
                    _state.value = _state.value.copy(
                        profile = _state.value.profile.copy(isEmailVerified = true),
                        message = "Email verified successfully! ✓"
                    )
                    try {
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .update("isEmailVerified", true)
                    } catch (_: Exception) {}
                    onResult(true, "Email address verified successfully! ✓")
                } else {
                    onResult(false, "Email not verified yet via link.")
                }
            } catch (_: Exception) {
                onResult(false, "Verification check failed.")
            }
        }
    }

    fun verifyEmailCode(inputCode: String, onResult: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val trimmedInput = inputCode.trim()
        val expectedCode = _state.value.generatedCode

        if (trimmedInput.isNotBlank() && expectedCode != null && trimmedInput == expectedCode) {
            markEmailVerifiedDirectly(onResult)
        } else {
            if (user != null) {
                viewModelScope.launch {
                    try {
                        user.reload().await()
                        if (user.isEmailVerified) {
                            markEmailVerifiedDirectly(onResult)
                            return@launch
                        }
                    } catch (_: Exception) {}
                    onResult(false, "Incorrect verification code. Please check your email inbox and try again.")
                }
            } else {
                onResult(false, "Incorrect verification code. Please check your email inbox and try again.")
            }
        }
    }

    fun updateProfile(newProfile: UserProfileDetails) {
        val emailLower = newProfile.email.trim().lowercase()
        prefs.edit().apply {
            putString("user_name", newProfile.fullName)
            putString("user_email", emailLower)
            putString("user_phone", newProfile.phone)
            putString("user_blood_group", newProfile.bloodGroup)
            putString("user_gender", newProfile.gender)
            putString("user_last_donation_date", newProfile.lastDonationDate)
            putString("user_dob", newProfile.dob)
            putString("user_country", newProfile.country)
            putString("user_city", newProfile.city)
            putString("user_img_uri", newProfile.imageUri)
            putString("user_desc", newProfile.shortDescription)
            putBoolean("user_email_verified", newProfile.isEmailVerified)
            if (emailLower.isNotBlank()) {
                putBoolean("user_email_verified_$emailLower", newProfile.isEmailVerified)
            }
            apply()
        }

        _state.value = _state.value.copy(
            profile = newProfile,
            message = "Profile details saved successfully!"
        )

        val currentUser = authManager.currentUser ?: FirebaseAuth.getInstance().currentUser
        val userUid = currentUser?.uid ?: if (emailLower.isNotBlank()) "user_${emailLower.hashCode()}" else null

        if (userUid != null) {
            viewModelScope.launch(Dispatchers.IO) {
                if (currentUser != null && newProfile.fullName.isNotBlank()) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(newProfile.fullName)
                            .build()
                        currentUser.updateProfile(profileUpdates).await()
                    } catch (_: Exception) {}
                }

                try {
                    val role = if (emailLower == "admin@gmail.com") "admin" else "user"
                    val map = hashMapOf(
                        "uid" to userUid,
                        "fullName" to newProfile.fullName,
                        "email" to newProfile.email,
                        "phone" to newProfile.phone,
                        "bloodGroup" to newProfile.bloodGroup,
                        "dob" to newProfile.dob,
                        "country" to newProfile.country,
                        "city" to newProfile.city,
                        "gender" to newProfile.gender,
                        "lastDonationDate" to newProfile.lastDonationDate,
                        "shortDescription" to newProfile.shortDescription,
                        "imageUri" to newProfile.imageUri,
                        "isEmailVerified" to newProfile.isEmailVerified,
                        "role" to role,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance().collection("users").document(userUid).set(map, SetOptions.merge()).await()
                } catch (_: Exception) {}
            }
        }
    }

    fun updateLastDonationDate(dateStr: String) {
        prefs.edit().putString("user_last_donation_date", dateStr).apply()
        _state.value = _state.value.copy(
            profile = _state.value.profile.copy(lastDonationDate = dateStr),
            message = "Last blood donation date updated to $dateStr!"
        )

        val user = authManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .update("lastDonationDate", dateStr)
                } catch (_: Exception) {}
            }
        }
    }

    fun addBloodDonationRecord(dateStr: String, hospital: String = "", notes: String = "") {
        val record = DonationRecord(
            id = UUID.randomUUID().toString(),
            donationDate = dateStr,
            locationOrHospital = hospital.ifBlank { "Local Hospital / Blood Bank" },
            notes = notes.ifBlank { "Blood donation" },
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.addDonationRecord(record)
            firestore.addDonationRecord(record)
            recalculateAndSaveLastDonationDate()
        }
    }

    fun updateBloodDonationRecord(record: DonationRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addDonationRecord(record)
            firestore.addDonationRecord(record)
            recalculateAndSaveLastDonationDate()
        }
    }

    fun deleteBloodDonationRecord(record: DonationRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteDonationRecord(record)
            firestore.deleteDonationRecord(record.id)
            recalculateAndSaveLastDonationDate()
        }
    }

    private fun recalculateAndSaveLastDonationDate() {
        viewModelScope.launch(Dispatchers.IO) {
            val records = dao.getDonationRecordsSync()
            val latestDate = records.map { it.donationDate }.filter { it.isNotBlank() }.maxOrNull() ?: ""
            withContext(Dispatchers.Main) {
                updateLastDonationDate(latestDate)
            }
        }
    }

    fun changePasswordManually(
        oldPass: String,
        newPass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = authManager.currentUser
        if (user == null || user.email.isNullOrBlank()) {
            onResult(false, "User is not signed in.")
            return
        }

        viewModelScope.launch {
            try {
                val email = user.email!!
                val credential = EmailAuthProvider.getCredential(email, oldPass)
                user.reauthenticate(credential).await()
                user.updatePassword(newPass).await()

                // Update in Firestore Database
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid).update(
                        mapOf(
                            "password" to newPass,
                            "passwordUpdatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                } catch (_: Exception) {}

                // Save in SharedPreferences
                prefs.edit().putString("saved_pass_${email.lowercase()}", newPass).apply()

                _state.value = _state.value.copy(message = "Password changed successfully!")
                onResult(true, "Password changed successfully!")
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password", ignoreCase = true) == true -> "Current password is incorrect."
                    e.message?.contains("weak", ignoreCase = true) == true -> "New password must be at least 6 characters."
                    else -> e.message ?: "Failed to update password."
                }
                onResult(false, msg)
            }
        }
    }

    fun addEmergencyContact(contact: EmergencyContactItem) {
        val updated = _state.value.emergencyContacts.toMutableList()
        updated.removeAll { it.id == contact.id }
        updated.add(0, contact)
        _state.value = _state.value.copy(
            emergencyContacts = updated,
            message = "Emergency contact ${contact.name} added!"
        )
        saveContactsToPrefs(updated)

        val user = authManager.currentUser
        if (user != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")
                        .document(contact.id)
                        .set(contact)
                        .await()
                } catch (_: Exception) {}
            }
        }
    }

    fun deleteEmergencyContact(id: String) {
        val updated = _state.value.emergencyContacts.filterNot { it.id == id }
        _state.value = _state.value.copy(
            emergencyContacts = updated,
            message = "Emergency contact removed."
        )

        val user = authManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .collection("emergency_contacts")
                        .document(id)
                        .delete()
                        .await()
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _state.value = _state.value.copy(notificationsEnabled = enabled)
    }

    fun toggleEmergencyAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("emergency_alerts_enabled", enabled).apply()
        _state.value = _state.value.copy(emergencyAlertsEnabled = enabled)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _state.value = _state.value.copy(language = lang)
    }

    fun setThemeMode(mode: String) {
        val isDark = mode == "dark"
        prefs.edit().putString("theme_mode", mode).putBoolean("dark_theme", isDark).apply()
        _state.value = _state.value.copy(
            themeMode = mode,
            isDarkTheme = isDark,
            message = when (mode) {
                "dark" -> "Dark Mode enabled."
                "light" -> "Light Mode enabled."
                else -> "Following Device System Theme."
            }
        )
    }

    fun toggleTheme(isDark: Boolean) {
        setThemeMode(if (isDark) "dark" else "light")
    }

    fun toggleAdminRole(enabled: Boolean) {
        val emailLower = _state.value.profile.email.trim().lowercase()
        if (emailLower.isNotBlank()) {
            prefs.edit().putBoolean("is_admin_role_$emailLower", enabled).apply()
        }
        _state.value = _state.value.copy(
            isAdminRole = enabled,
            message = if (enabled) "⚡ Admin Moderator Role Enabled!" else "Regular User Role Enabled."
        )

        val user = authManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid)
                        .update("role", if (enabled) "admin" else "user").await()
                } catch (_: Exception) {}
            }
        }
    }

    fun sendPasswordResetEmail() {
        val email = _state.value.profile.email
        if (email.isNotBlank()) {
            viewModelScope.launch {
                try {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                    _state.value = _state.value.copy(message = "Password reset email sent to $email")
                } catch (e: Exception) {
                    _state.value = _state.value.copy(message = e.message ?: "Failed to send reset email.")
                }
            }
        } else {
            _state.value = _state.value.copy(message = "No registered email found.")
        }
    }

    fun signOut() {
        try {
            userSnapshotRegistration?.remove()
            userSnapshotRegistration = null
            contactsSnapshotRegistration?.remove()
            contactsSnapshotRegistration = null

            prefs.edit().apply {
                remove("user_name")
                remove("user_email")
                remove("user_phone")
                remove("user_blood_group")
                remove("user_gender")
                remove("user_last_donation_date")
                remove("user_dob")
                remove("user_country")
                remove("user_city")
                remove("user_desc")
                remove("user_img_uri")
                remove("user_email_verified")
                apply()
            }
            authManager.signOut()
        } catch (_: Exception) {}

        _state.value = SettingsState()
        loadSettings()
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = authManager.currentUser
                user?.delete()?.await()
                signOut()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = e.message ?: "Failed to delete account.")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
