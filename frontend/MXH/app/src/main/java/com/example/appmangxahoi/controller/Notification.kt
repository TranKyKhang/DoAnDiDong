package com.example.appmangxahoi.controller

import android.util.Log
import com.example.appmangxahoi.model.DataClassNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Notification {

    private val client = OkHttpClient()
    private val URL = "http://10.0.2.2:3000"

    suspend fun postNotification(
        token: String,
        type: String,
        content: String,
        receiverId: Int,
        postId: Int? = null,
        commentId: Int? = null,
        senderId: Int? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("type", type)
                put("recipient_id", receiverId)
                put("content", content)
                put("sender_id", senderId)
                if (postId != null) put("post_id", postId)
                if (commentId != null) put("comment_id", commentId)
            }

            val requestBody = json
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val req = Request.Builder()
                .url("$URL/api/notifications")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("NotificationErr", "POST lỗi: ${resp.code}")
                }
                return@withContext resp.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getNotifications(token: String): List<DataClassNotification>? =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$URL/api/notifications/noti")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null

                    val body = resp.body?.string() ?: return@withContext null
                    val obj = JSONObject(body)
                    val dataArray = obj.optJSONArray("data") ?: return@withContext emptyList()

                    val jsonParser = Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }

                    val notifications = mutableListOf<DataClassNotification>()
                    for (i in 0 until dataArray.length()) {
                        try {
                            val itemStr = dataArray[i].toString()
                            val item = jsonParser.decodeFromString<DataClassNotification>(itemStr)
                            notifications.add(item)
                        } catch (e: Exception) {
                            Log.e("NotificationErr", "Parse item $i lỗi: ${e.message}")
                        }
                    }
                    return@withContext notifications
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    suspend fun updateRead(
        token: String,
        notificationId: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$URL/api/notifications/isread/$notificationId")
                .addHeader("Authorization", "Bearer $token")
                .patch("".toRequestBody(null))
                .build()

            client.newCall(req).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
