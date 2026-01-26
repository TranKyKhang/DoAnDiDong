package com.example.appmangxahoi.controller

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.model.PostResponse
import com.example.appmangxahoi.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class post {
    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getPopularPosts(
        context: Context,
        page: Int,
        limit: Int = 20
    ): List<PostModel>? =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", "Token bị null")
                return@withContext null
            }
            try {
                val url = "$BASE_URL/api/posts/popular?page=$page&limit=$limit"
                Log.d("API_DEBUG", "Gọi API: $url")
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(
                            "API_DEBUG",
                            "Lỗi Server: ${response.code} - ${response.message}"
                        )
                        return@withContext null
                    }
                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", "Body rỗng")
                        return@withContext null
                    }
                    Log.d("API_DEBUG", "JSON trả về: $body")
                    val listPosts = json
                        .decodeFromString<PostResponse>(body)
                        .data
                    Log.d(
                        "API_DEBUG",
                        " Page $page: ${listPosts.size} bài"
                    )
                    return@withContext listPosts
                }
            } catch (e: Exception) {
                Log.e(
                    "API_DEBUG",
                    "Crash khi gọi API / parse JSON: ${e.message}"
                )
                e.printStackTrace()
                null
            }
        }

    suspend fun getMyPosts(
        context: Context,
        page: Int,
        limit: Int = 20
    ): List<PostModel>? =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                return@withContext null
            }
            try {
                val url = "$BASE_URL/api/posts/my-posts?page=$page&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(
                            "API_DEBUG",
                            "ỗi Server: Code ${response.code} - ${response.message}"
                        )
                        return@withContext null
                    }
                    val body = response.body?.string()
                    if (body == null) {
                        return@withContext null
                    }
                    val responseObj = json.decodeFromString<PostResponse>(body)
                    return@withContext responseObj.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun getFollowedPosts(
        context: Context,
        page: Int
    ): List<PostModel>? =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", "Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }
            try {
                val url = "$BASE_URL/api/posts/followed?page=$page"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(
                            "API_DEBUG",
                            "Server lỗi: ${response.code} - ${response.message}"
                        )
                        return@withContext null
                    }
                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", "body rỗng")
                        return@withContext null
                    }
                    Log.d("API_DEBUG", "JSON: $body")
                    val listPosts =
                        json.decodeFromString<PostResponse>(body).data
                    Log.d(
                        "API_DEBUG",
                        "Page $page: ${listPosts.size} bài viết"
                    )
                    return@withContext listPosts
                }
            } catch (e: Exception) {
                Log.e(
                    "API_DEBUG",
                    " Crash API/Parse: ${e.message}"
                )
                e.printStackTrace()
                null
            }
        }

    suspend fun getCommunityPosts(
        context: Context,
        id: Int,
        page: Int,
        limit: Int = 20
    ): List<PostModel>? =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                return@withContext null
            }
            try {
                val url = "$BASE_URL/api/posts/community/$id?page=$page&limit=$limit"
                Log.d("API_DEBUG", " Gọi API: $url")
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext null
                    }
                    val body = response.body?.string()
                    if (body == null) {
                        return@withContext null
                    }
                    val listPosts = json
                        .decodeFromString<PostResponse>(body)
                        .data
                    return@withContext listPosts
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun getNewestCommunityPosts(
        context: Context,
        id: Int,
        page: Int,
        limit: Int = 20
    ): List<PostModel>? = withContext(Dispatchers.IO) {
        val token = TokenManager.getToken(context) ?: return@withContext null
        try {
            val url = "$BASE_URL/api/posts/community/$id/newest?page=$page&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                json.decodeFromString<PostResponse>(body).data
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRisingCommunityPosts(
        context: Context,
        id: Int,
        page: Int,
        limit: Int = 20
    ): List<PostModel>? = withContext(Dispatchers.IO) {
        val token = TokenManager.getToken(context) ?: return@withContext null
        try {
            val url = "$BASE_URL/api/posts/community/$id/rising?page=$page&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                json.decodeFromString<PostResponse>(body).data
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun handleVote(
        context: Context,
        postId: Int,
        type: String
    ) = withContext(Dispatchers.IO) {
        val token = TokenManager.getToken(context) ?: return@withContext
        val json = """
        {
            "type": "$type"
        }
    """.trimIndent()
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/api/votes/post/$postId")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            Log.d("API_DEBUG", "Vote response: ${response.code}")
        }
    }

    suspend fun createPost(
        context: Context,
        title: String,
        content: String?,
        communityId: Int?,
        imageUris: List<Uri> = emptyList(),
        videoUri: Uri? = null,
        linkUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = TokenManager.getToken(context)
                ?: return@withContext false
            val bodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            // ===== TEXT =====
            bodyBuilder.addFormDataPart("title", title)
            content?.let {
                bodyBuilder.addFormDataPart("content", it)
            }
            communityId?.let {
                bodyBuilder.addFormDataPart("community_id", it.toString())
            }
            linkUrl?.let {
                bodyBuilder.addFormDataPart("link_url", it)
            }
            imageUris.forEach { uri ->
                val file = uriToFile(context, uri)
                bodyBuilder.addFormDataPart(
                    "images",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
            }
            videoUri?.let { uri ->
                val file = uriToFile(context, uri)
                bodyBuilder.addFormDataPart(
                    "video",
                    file.name,
                    file.asRequestBody("video/*".toMediaType())
                )
            }
            val request = Request.Builder()
                .url("$BASE_URL/api/posts/create")
                .post(bodyBuilder.build())
                .addHeader("Authorization", "Bearer $token")
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun uriToFile(context: Context, uri: Any): File {
        val realUri = uri as Uri
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(realUri)
        val extension = when (mimeType) {
            "image/png" -> ".png"
            "image/jpeg", "image/jpg" -> ".jpg"
            "image/webp" -> ".webp"
            "video/mp4" -> ".mp4"
            else -> ""
        }
        val inputStream = contentResolver.openInputStream(realUri)!!
        val file = File(
            context.cacheDir,
            "upload_${System.currentTimeMillis()}$extension"
        )
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}