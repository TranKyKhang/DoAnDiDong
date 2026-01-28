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

    /* =======================
       GET COMMENTS (PUBLIC)
       ======================= */
    suspend fun getComments(token: String, postID: Int): List<DataClassComment>? =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$URL/api/comments/post/$postID")
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

                    val rootComments = mutableListOf<DataClassComment>()
                    for (i in 0 until dataArray.length()) {
                        try {
                            val commentStr = dataArray[i].toString()
                            val comment =
                                jsonParser.decodeFromString<DataClassComment>(commentStr)
                            rootComments.add(comment)
                        } catch (e: Exception) {
                            Log.e("CommentErr", "Parse item $i lỗi: ${e.message}")
                        }
                    }

                    return@withContext flattenComments(rootComments)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    /* =======================
       GET SINGLE COMMENT
       ======================= */
    suspend fun getCommentById(token: String, commentId: Int): DataClassComment? =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$URL/api/comments/$commentId")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body?.string() ?: return@withContext null

                    val obj = JSONObject(body)
                    val data = obj.optJSONObject("data") ?: return@withContext null

                    val jsonParser = Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                    val detail = jsonParser.decodeFromString<DataClassComment>(data.toString())
                    return@withContext detail
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    /* =======================
       POST COMMENT (TOKEN)
       ======================= */
    suspend fun postComment(
        token: String,
        postId: Int,
        content: String,
        parentCommentId: Int? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("post_id", postId)
                put("content", content)
                if (parentCommentId != null) {
                    put("parent_id", parentCommentId)
                }
            }

            val requestBody = json
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val req = Request.Builder()
                .url("$URL/api/comments")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(req).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /* =======================
       FLATTEN COMMENT TREE
       ======================= */
    private fun flattenComments(
        comments: List<DataClassComment>
    ): List<DataClassComment> {
        val result = mutableListOf<DataClassComment>()
        for (comment in comments) {
            result.add(comment)
            if (comment.children.isNotEmpty()) {
                result.addAll(flattenComments(comment.children))
            }
        }
        return result
    }
}
