package com.bytecoder.vplay.utils

import android.content.Context
import android.content.SharedPreferences

class LastTabStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vplay_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_TAB = "last_tab_index"
        const val DEFAULT_TAB_INDEX = 1 // 0 = Music, 1 = Video
    }

    fun saveLastTab(index: Int) {
        prefs.edit().putInt(KEY_LAST_TAB, index).apply()
    }

    fun getLastTab(): Int {
        return prefs.getInt(KEY_LAST_TAB, DEFAULT_TAB_INDEX)
    }
}
