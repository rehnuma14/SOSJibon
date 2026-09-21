package com.example.sosjibon.ai

import com.example.sosjibon.elibrary.resources.data.EmergencyCondition
import com.example.sosjibon.elibrary.resources.data.FirstAidContent

object FirstAidMatcher {
    fun match(query: String): EmergencyCondition? {
        val q = query.lowercase()
        return FirstAidContent.allConditions.firstOrNull { condition ->
            condition.searchTags.any { tag -> q.contains(tag.lowercase()) } ||
                    q.contains(condition.title.lowercase()) ||
                    q.contains(condition.titleBn)
        }
    }
}