package com.example.nexora1.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "nexora_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_ONBOARDING_DONE = "is_onboarding_done"
    }

    fun saveSession(token: String, username: String, email: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun setOnboardingDone(isDone: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDING_DONE, isDone).apply()
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_IS_ONBOARDING_DONE, false)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, "User")

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, "user@nexora.com")

    fun logout() {
        prefs.edit().clear().apply()
        // Keep onboarding status even after logout? Usually yes.
        setOnboardingDone(true) 
    }
}