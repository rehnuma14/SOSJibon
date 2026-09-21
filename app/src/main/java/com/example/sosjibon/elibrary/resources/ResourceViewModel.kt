package com.example.sosjibon.elibrary.resources

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sosjibon.elibrary.resources.data.BookmarkStore
import com.example.sosjibon.elibrary.resources.data.EmergencyCondition
import com.example.sosjibon.elibrary.resources.data.FirstAidContent
import com.example.sosjibon.elibrary.resources.data.GuideCategory
import com.example.sosjibon.elibrary.resources.data.SeverityTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResourceViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<GuideCategory?>(null)
    val selectedCategory: StateFlow<GuideCategory?> = _selectedCategory

    private val _severityFilter = MutableStateFlow<SeverityTag?>(null)
    val severityFilter: StateFlow<SeverityTag?> = _severityFilter

    private val _bookmarkedIds = MutableStateFlow(BookmarkStore.getBookmarkedIds(application))
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarkedIds

    val mostUrgent: List<EmergencyCondition> = FirstAidContent.mostUrgent

    val savedConditions: StateFlow<List<EmergencyCondition>> = combine(
        _bookmarkedIds, _searchQuery
    ) { bookmarked, query ->
        FirstAidContent.allConditions.filter { condition ->
            bookmarked.contains(condition.id) && (query.isBlank() || matchesSearch(condition, query))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredConditions: StateFlow<List<EmergencyCondition>> = combine(
        _searchQuery, _selectedCategory, _severityFilter, _bookmarkedIds
    ) { query, category, severity, bookmarked ->
        FirstAidContent.allConditions
            .filter { condition ->
                val matchesQuery = query.isBlank() || matchesSearch(condition, query)
                val matchesCategory = category == null || condition.category == category
                val matchesSeverity = severity == null || condition.severity == severity
                matchesQuery && matchesCategory && matchesSeverity
            }
            .sortedByDescending { bookmarked.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FirstAidContent.allConditions
    )

    val hasNoSearchResults: StateFlow<Boolean> = combine(
        _searchQuery, filteredConditions
    ) { query, results ->
        query.isNotBlank() && results.isEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private fun matchesSearch(condition: EmergencyCondition, query: String): Boolean {
        val q = query.trim().lowercase()
        if (condition.title.lowercase().contains(q)) return true
        return condition.searchTags.any { tag -> tag.lowercase().contains(q) || q.contains(tag.lowercase()) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: GuideCategory?) {
        _selectedCategory.value = category
    }

    fun onSeverityFilterChanged(severity: SeverityTag?) {
        _severityFilter.value = severity
    }

    fun toggleBookmark(conditionId: String) {
        viewModelScope.launch {
            BookmarkStore.toggleBookmark(getApplication(), conditionId)
            _bookmarkedIds.value = BookmarkStore.getBookmarkedIds(getApplication())
        }
    }

    fun isBookmarked(conditionId: String): Boolean = _bookmarkedIds.value.contains(conditionId)

    fun getCondition(id: String): EmergencyCondition? = FirstAidContent.getById(id)
}
