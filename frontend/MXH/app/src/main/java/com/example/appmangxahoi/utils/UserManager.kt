package com.example.appmangxahoi.utils

import android.content.Context
import android.util.Base64
import org.json.JSONObject

object UserManager {

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
}
