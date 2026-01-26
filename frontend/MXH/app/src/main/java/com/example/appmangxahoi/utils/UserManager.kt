package com.example.appmangxahoi.utils

import android.content.Context
import android.util.Base64
import com.example.appmangxahoi.controller.UserController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.core.content.edit

object UserManager {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_LOGIN_METHOD = "login_method"

    fun getUserId(context: Context): Int? {
        val token = TokenManager.getToken(context) ?: return null

        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = String(
                Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )
            )

            val json = JSONObject(payload)
            json.getInt("id") //  key phải khớp backend
        } catch (e: Exception) {
            null
        }
    }

    fun getUsername(context: Context): String? {
        val token = TokenManager.getToken(context) ?: return null

        return try {
            val payload = String(
                Base64.decode(token.split(".")[1],
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )

            JSONObject(payload).optString("username")
        } catch (e: Exception) {
            null
        }
    }

    fun saveLoginMethod(context: Context, method: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LOGIN_METHOD, method) }
        println("UserManager: saved login_method = $method")
    }

    fun getLoginMethod(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val method = prefs.getString(KEY_LOGIN_METHOD, "email") ?: "email"
        println("UserManager: get login_method = $method")
        return method
    }
}
