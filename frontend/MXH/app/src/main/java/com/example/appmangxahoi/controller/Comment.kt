package com.example.appmangxahoi.controller

import android.util.Log
import com.example.appmangxahoi.model.DataClassComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Comment {
    private val client = OkHttpClient()
    private val URL = "http://10.0.2.2:3000"

    suspend fun getComments(postID: Int): List<DataClassComment>? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$URL/api/comments/post/$postID")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null

                // 1. Parse JSON lấy mảng "data" gốc
                val obj = JSONObject(body)
                val dataArray = obj.optJSONArray("data") ?: return@withContext emptyList()

                val jsonParser = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }

                // 2. Parse mảng gốc thành List các Root Comment (chứa children bên trong)
                val rootComments = mutableListOf<DataClassComment>()
                for (i in 0 until dataArray.length()) {
                    try {
                        val commentStr = dataArray[i].toString()
                        val comment = jsonParser.decodeFromString<DataClassComment>(commentStr)
                        rootComments.add(comment)
                    } catch (e: Exception) {
                        Log.e("CommentErr", "Lỗi parse item $i: ${e.message}")
                    }
                }

                // 3. Gọi hàm đệ quy để làm phẳng danh sách trước khi trả về
                return@withContext flattenComments(rootComments)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    suspend fun postComment(
        postId: Int,
        userId: Int,
        content: String,
        parentCommentId: Int? = null
    ): Boolean = withContext(Dispatchers.IO){
        try {
            val json = JSONObject().apply {
                put("post_id", postId)
                put("user_id", userId)
                put("content", content)
                if (parentCommentId != null) {
                    put("parent_id", parentCommentId) // Backend cần trường này để biết là reply
                }
            }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val req = Request.Builder()
                .url("$URL/api/comments")
                .post(requestBody)
                .build()
            client.newCall(req).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    private fun flattenComments(comments: List<DataClassComment>): List<DataClassComment> {
        val result = mutableListOf<DataClassComment>()
        for (comment in comments) {
            // Thêm cha vào list
            result.add(comment)

            // Nếu có con, gọi đệ quy để lấy hết con cháu chắt ra
            if (comment.children.isNotEmpty()) {
                result.addAll(flattenComments(comment.children))
            }
        }
        return result
    }
}