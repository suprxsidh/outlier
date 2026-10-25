package com.outlier.samplespace.ui

import android.content.Context
import org.json.JSONArray

interface SetupStore {
    fun load(): SetupState?
    fun save(setup: SetupState)
}

class PrefsSetupStore(context: Context) : SetupStore {
    private val prefs = context.applicationContext.getSharedPreferences("outlier_setup", Context.MODE_PRIVATE)

    override fun load(): SetupState? = runCatching {
        val names = prefs.getString(KEY_NAMES, null)?.let { raw ->
            JSONArray(raw).let { array -> List(array.length()) { array.getString(it) } }
        } ?: return null
        val count = prefs.getInt(KEY_COUNT, names.size).coerceIn(4, 15)
        SetupState(
            playerCount = count,
            undercoverCount = prefs.getInt(KEY_UNDERCOVER, DEFAULT_UNDERCOVER_COUNT),
            mrWhiteCount = prefs.getInt(KEY_MR_WHITE, DEFAULT_MR_WHITE_COUNT),
            playerNames = List(count) { names.getOrElse(it) { "" } },
            selectedCategory = prefs.getString(KEY_CATEGORY, "All") ?: "All",
            countPartsEnabled = prefs.getBoolean(KEY_COUNT_PARTS, false)
        )
    }.getOrNull()

    override fun save(setup: SetupState) {
        prefs.edit()
            .putString(KEY_NAMES, JSONArray(setup.playerNames).toString())
            .putInt(KEY_COUNT, setup.playerCount)
            .putInt(KEY_UNDERCOVER, setup.undercoverCount)
            .putInt(KEY_MR_WHITE, setup.mrWhiteCount)
            .putString(KEY_CATEGORY, setup.selectedCategory)
            .putBoolean(KEY_COUNT_PARTS, setup.countPartsEnabled)
            .apply()
    }

    private companion object {
        const val KEY_NAMES = "names"
        const val KEY_COUNT = "player_count"
        const val KEY_UNDERCOVER = "undercover"
        const val KEY_MR_WHITE = "mr_white"
        const val KEY_CATEGORY = "category"
        const val KEY_COUNT_PARTS = "count_parts"
    }
}
