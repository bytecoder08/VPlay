package com.bytecoder.vplay.utils

import android.content.Context
import android.content.SharedPreferences

// ---------------- ENUMS ----------------

enum class TabType(val index: Int, val key: String) {
    VIDEO(0, "video"),
    MUSIC(1, "music"),
    PLAYLIST(2, "playlist"),
    ONLINE(3, "online"),
    OPTIONS(4, "options");

    companion object {
        fun fromIndex(index: Int): TabType {
            return values().find { it.index == index } ?: VIDEO // default
        }
    }
}

enum class SortOrder(val value: String) {
    NAME("name"),
    DATE("date"),
    DURATION("duration");

    companion object {
        fun fromValue(value: String): SortOrder {
            return values().find { it.value == value } ?: NAME
        }
    }
}

enum class ViewMode(val value: String) {
    LIST("list"),
    GRID("grid");

    companion object {
        fun fromValue(value: String): ViewMode {
            return values().find { it.value == value } ?: LIST
        }
    }
}

// ---------------- STORE PREFERENCE ----------------

class StorePreference(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vplay_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_TAB = "last_tab_index"
        private const val KEY_SORT_ORDER_PREFIX = "sort_order_"     // + fragment name
        private const val KEY_VIEW_MODE_PREFIX = "view_mode_"       // + fragment name
    }

    // -------- TAB --------
    fun saveLastTab(tab: TabType) {
        prefs.edit().putInt(KEY_LAST_TAB, tab.index).apply()
    }

    fun getLastTab(): TabType {
        val index = prefs.getInt(KEY_LAST_TAB, TabType.VIDEO.index)
        return TabType.fromIndex(index)
    }

    // -------- SORT ORDER --------
    fun saveSortOrder(tab: TabType, sortOrder: SortOrder) {
        prefs.edit().putString(KEY_SORT_ORDER_PREFIX + tab.key, sortOrder.value).apply()
    }

    fun getSortOrder(tab: TabType): SortOrder {
        val value = prefs.getString(KEY_SORT_ORDER_PREFIX + tab.key, SortOrder.NAME.value)!!
        return SortOrder.fromValue(value)
    }

    // -------- VIEW MODE --------
    fun saveViewMode(tab: TabType, viewMode: ViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE_PREFIX + tab.key, viewMode.value).apply()
    }

    fun getViewMode(tab: TabType): ViewMode {
        val value = prefs.getString(KEY_VIEW_MODE_PREFIX + tab.key, ViewMode.LIST.value)!!
        return ViewMode.fromValue(value)
    }
}
