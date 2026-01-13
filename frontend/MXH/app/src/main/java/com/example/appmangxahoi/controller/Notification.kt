package com.example.appmangxahoi.controller

import android.util.Log
import com.example.appmangxahoi.model.DataClassComment
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

class Notification{
    private val client = OkHttpClient()
    private val URL = "http://10.0.2.2:3000"
    suspend fun postNotification(
        type: String,
        content: String,
        recipientId: Int,
        senderId: Int,
        postId: Int? = null,
        commentId: Int? = null
    ): Boolean = withContext(Dispatchers.IO){
        try {
            val json = JSONObject().apply {
                put("type", type)
                put("content", content)
                put("recipient_id", recipientId)
                put("sender_id", senderId)
                if (postId != null) put("post_id", postId)
                if (commentId != null) put("comment_id", commentId)
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val req = Request.Builder()
                .url("$URL/api/notifications")
                .post(requestBody)
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("NotificationErr", "Lỗi tạo thông báo: ${resp.code} - ${resp.message}")
                }
                return@withContext resp.isSuccessful
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    suspend fun getNotification(id: Int): List<DataClassNotification>? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$URL/api/notifications/$id")
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

                val rootNotification = mutableListOf<DataClassNotification>()
                for (i in 0 until dataArray.length()) {
                    try {
                        val notificationStr = dataArray[i].toString()
                        val notification = jsonParser.decodeFromString<DataClassNotification>(notificationStr)
                        rootNotification.add(notification)
                    } catch (e: Exception) {
                        Log.e("NotificationErr", "Lỗi parse item $i: ${e.message}")
                    }
                }
                return@withContext rootNotification
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    suspend fun patchRead(id: Int): Boolean = withContext(Dispatchers.IO){
        try {
            val req = Request.Builder()
                .url("$URL/api/notifications/read/$id")
                .patch(RequestBody.create(null, ByteArray(0)))
                .build()
            client.newCall(req).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}