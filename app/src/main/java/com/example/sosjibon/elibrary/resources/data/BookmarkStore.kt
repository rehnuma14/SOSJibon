package com.example.sosjibon.elibrary.resources.data

import android.content.Context

object BookmarkStore {
    private const val PREFS_NAME = "sosjibon_elibrary_bookmarks"
    private const val KEY_BOOKMARKS = "bookmarked_ids"

    fun getBookmarkedIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
    }

    fun isBookmarked(context: Context, conditionId: String): Boolean =
        getBookmarkedIds(context).contains(conditionId)

    fun toggleBookmark(context: Context, conditionId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getBookmarkedIds(context).toMutableSet()
        val nowBookmarked: Boolean
        if (current.contains(conditionId)) {
            current.remove(conditionId)
            nowBookmarked = false
        } else {
            current.add(conditionId)
            nowBookmarked = true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, HashSet(current)).apply()
        return nowBookmarked
    }
}
