package com.example.sosjibon.ai

import android.content.Context

object ProfileLookup {
    private const val PREFS_NAME = "sosjibon_profile_settings"

    data class Field(val label: String, val prefsKey: String, val default: String)

    private val FIELDS = listOf(
        Field("Blood Group", "user_blood_group", "O+"),
        Field("Phone Number", "user_phone", ""),
        Field("City", "user_city", "Dhaka"),
        Field("Country", "user_country", "Bangladesh"),
        Field("Date of Birth", "user_dob", "")
    )

    private val TRIGGERS: Map<String, Int> = mapOf(
        "blood group" to 0, "amar blood group" to 0, "রক্তের গ্রুপ" to 0,
        "phone" to 1, "amar number" to 1,
        "city" to 2, "country" to 3,
        "date of birth" to 4, "dob" to 4
    )

    fun find(context: Context, query: String): Field? {
        val q = query.lowercase()
        val idx = TRIGGERS.entries.firstOrNull { q.contains(it.key) }?.value ?: return null
        return FIELDS[idx]
    }

    fun valueOf(context: Context, field: Field): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(field.prefsKey, field.default) ?: field.default
    }
}