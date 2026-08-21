package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeModeFromPrefs())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _targetAppColorHex = MutableStateFlow(getAppColorFromPrefs())
    val targetAppColorHex: StateFlow<String> = _targetAppColorHex.asStateFlow()

    private val _globalBudgetLimit = MutableStateFlow(getGlobalBudgetLimitFromPrefs())
    val globalBudgetLimit: StateFlow<Long> = _globalBudgetLimit.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("THEME_MODE", mode.name).apply()
        _themeMode.value = mode
    }

    fun setAppColorHex(hex: String) {
        prefs.edit().putString("APP_COLOR_HEX", hex).apply()
        _targetAppColorHex.value = hex
    }

    fun setGlobalBudgetLimit(limit: Long) {
        prefs.edit().putLong("GLOBAL_BUDGET_LIMIT", limit).apply()
        _globalBudgetLimit.value = limit
    }

    private fun getAppColorFromPrefs(): String {
        return prefs.getString("APP_COLOR_HEX", "") ?: ""
    }

    private fun getGlobalBudgetLimitFromPrefs(): Long {
        return prefs.getLong("GLOBAL_BUDGET_LIMIT", 0L)
    }

    private fun getThemeModeFromPrefs(): ThemeMode {
        val modeStr = prefs.getString("THEME_MODE", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
