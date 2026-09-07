package com.example.sosjibon.elibrary.resources.data

import androidx.annotation.DrawableRes

enum class GuideCategory(val displayName: String) {
    FIRST_AID("First Aid"),
    BREATHING_CONSCIOUSNESS("Breathing & Consciousness"),
    MEDICAL_CONDITIONS("Medical Conditions"),
    POISON_BITES("Poison & Bites"),
    ACCIDENTS("Accidents")
}

enum class SeverityTag {
    RED, BLUE, GREEN
}

data class GuideStep(
    val stepNumber: Int,
    val instruction: String,
    @DrawableRes val imageRes: Int,
    val imageDescription: String
)

data class EmergencyCondition(
    val id: String,
    val title: String,
    val titleBn: String,
    val category: GuideCategory,
    @DrawableRes val cardIconRes: Int,
    val severity: SeverityTag,

    val whatHappened: String,
    val recognizeSigns: List<String>,
    val doThis: List<GuideStep>,
    val dontDoThis: List<String>,
    val whenToGetHelp: String,

    val searchTags: List<String> = emptyList(),
    val relatedConditionIds: List<String> = emptyList(),

    val videoUrl: String? = null,
    val lastReviewedBy: String? = null,
    val lastReviewedDate: String? = null,

    val isMostUrgent: Boolean = false
)
