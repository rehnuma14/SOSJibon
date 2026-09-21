package com.example.sosjibon.ai

class LocalRetriever {

    fun search(query: String): String? {

        val cleanedQuery = query
            .trim()
            .lowercase()

        if (cleanedQuery.isEmpty()) {
            return null
        }

        return null
    }
}