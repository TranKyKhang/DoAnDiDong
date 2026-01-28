package com.example.appmangxahoi.controller

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Vote {

    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000/api/votes"

    suspend fun getVoteStatus(
        token: String,
        target: String,
        id: Int
    ): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/$target/$id")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext ""

                val body = resp.body?.string() ?: return@withContext ""
                val obj = JSONObject(body)
                return@withContext obj.optString("type", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun postVote(
        token: String,
        target: String,
        id: Int,
        type: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("type", type)
            }

            val body = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val req = Request.Builder()
                .url("$BASE_URL/$target/$id")
                .addHeader("Authorization", "Bearer $token")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("API_VOTE", "Vote fail: ${resp.code}")
                    return@withContext false
                }

                val respBody = resp.body?.string() ?: return@withContext false
                val obj = JSONObject(respBody)
                return@withContext obj.optBoolean("success", false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
