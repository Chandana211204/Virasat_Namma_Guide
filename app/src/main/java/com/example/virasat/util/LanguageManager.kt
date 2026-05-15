package com.example.virasat.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {
    private const val PREF_NAME = "virasat_language"
    private const val KEY_LANGUAGE = "language"

    const val ENGLISH = "en"
    const val KANNADA = "kn"

    fun currentLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    fun isKannada(context: Context): Boolean = currentLanguage(context) == KANNADA

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
    }

    fun applySavedLanguage(context: Context) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(currentLanguage(context))
        )
    }
}
