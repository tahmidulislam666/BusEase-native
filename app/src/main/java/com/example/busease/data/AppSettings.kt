package com.example.busease.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা")
}

class AppSettings private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("busease_user_settings", Context.MODE_PRIVATE)

    // Dark mode is OFF by default as requested: "Do not use dark mode rather set a option in the settings dark mode on off"
    private var _isDarkMode = mutableStateOf(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: Boolean get() = _isDarkMode.value

    private var _appLanguage = mutableStateOf(
        if (prefs.getString(KEY_LANGUAGE, "en") == "bn") AppLanguage.BANGLA else AppLanguage.ENGLISH
    )
    val appLanguage: AppLanguage get() = _appLanguage.value

    fun updateDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun updateLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    companion object {
        private const val KEY_DARK_MODE = "busease_dark_mode_active"
        private const val KEY_LANGUAGE = "busease_selected_language"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
