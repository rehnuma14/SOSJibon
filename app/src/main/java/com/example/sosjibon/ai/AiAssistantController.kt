package com.example.sosjibon.ai

class AiAssistantController(
    private val intentClassifier: IntentClassifier,
    private val localRetriever: LocalRetriever = LocalRetriever()
) {

    fun handleQuery(query: String): AiResult {

        val cleanedQuery = query.trim()

        if (cleanedQuery.isEmpty()) {
            return AiResult(
                intent = AiIntent.UNKNOWN,
                message = "Please enter a question."
            )
        }

        val intent = intentClassifier.classify(cleanedQuery)

        return when (intent) {

            AiIntent.HOME -> {
                AiResult(
                    intent = intent,
                    message = "Opening Home."
                )
            }

            AiIntent.RESOURCES -> {
                AiResult(
                    intent = intent,
                    message = "Opening Health Resources."
                )
            }

            AiIntent.VAULT -> {
                AiResult(
                    intent = intent,
                    message = "Opening your Medical Vault."
                )
            }

            AiIntent.SETTINGS -> {
                AiResult(
                    intent = intent,
                    message = "Opening Settings."
                )
            }

            AiIntent.EMERGENCY_SOS -> {
                AiResult(
                    intent = intent,
                    message = "Opening Emergency SOS."
                )
            }

            AiIntent.GPS_MAP -> {
                AiResult(
                    intent = intent,
                    message = "Opening GPS Map."
                )
            }

            AiIntent.COMMUNITY_STORIES -> {
                AiResult(
                    intent = intent,
                    message = "Opening Community Stories."
                )
            }

            AiIntent.EDIT_PROFILE -> {
                AiResult(
                    intent = intent,
                    message = "Opening Edit Profile."
                )
            }

            AiIntent.SECURITY -> {
                AiResult(
                    intent = intent,
                    message = "Opening Security."
                )
            }

            AiIntent.PRIVACY_TERMS -> {
                AiResult(
                    intent = intent,
                    message = "Opening Privacy and Terms."
                )
            }

            AiIntent.DEVELOPERS -> {
                AiResult(
                    intent = intent,
                    message = "Opening Developers."
                )
            }

            AiIntent.EMERGENCY_CONTACTS -> {
                AiResult(
                    intent = intent,
                    message = "Opening Emergency Contacts."
                )
            }

            AiIntent.RESOURCE_ASSESSMENT -> {
                AiResult(
                    intent = intent,
                    message = "Opening Assessment."
                )
            }

            AiIntent.FIRST_AID -> {

                val localAnswer =
                    localRetriever.search(cleanedQuery)

                AiResult(
                    intent = intent,
                    message = localAnswer
                        ?: "I found this as a first-aid question, but I don't have a matching first-aid instruction yet."
                )
            }

            AiIntent.UNKNOWN -> {

                val localAnswer =
                    localRetriever.search(cleanedQuery)

                if (localAnswer != null) {
                    AiResult(
                        intent = AiIntent.UNKNOWN,
                        message = localAnswer
                    )

                } else {

                    AiResult(
                        intent = AiIntent.UNKNOWN,
                        message = "I could not understand that yet. Please try asking about SOS, GPS, Resources, Medical Vault, Settings, Emergency Contacts or First Aid."
                    )
                }
            }
        }
    }
}

data class AiResult(
    val intent: AiIntent,
    val message: String
)