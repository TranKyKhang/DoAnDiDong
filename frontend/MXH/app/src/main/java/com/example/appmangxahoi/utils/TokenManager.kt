package com.example.appmangxahoi.utils

import android.content.Context
import androidx.core.content.edit

object TokenManager {

    private const val PREF_NAME = "auth_prefs"
    private const val TOKEN_KEY = "jwt_token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(TOKEN_KEY, token) }
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(TOKEN_KEY) }
    }
}
