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
import kotlin.collections.forEachIndexed

class post {
    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"
    private val json = Json { ignoreUnknownKeys = true }



    // 1. Cấu hình JSON (QUAN TRỌNG: Phải để ignoreUnknownKeys = true)
// Nếu server trả về trường lạ mà không có config này -> Crash ngay lập tức -> Nhảy vào catch

    suspend fun getPopularPosts(context: Context): List<PostModel>? =
        withContext(Dispatchers.IO) {
            // Log 1: Kiểm tra Token
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/posts/popular")

                val request = Request.Builder()
                    .url("$BASE_URL/api/posts/popular")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    // Log 2: Kiểm tra HTTP Code
                    if (!response.isSuccessful) {
                        Log.e("API_DEBUG", " Lỗi Server: Code ${response.code} - ${response.message}")
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }

                    // Log 3: In ra JSON thô mà Server trả về
                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    // Log 4: Thử Parse
                    val listPosts = json.decodeFromString<PostResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listPosts.size} bài viết")

                    return@withContext listPosts
                }
            } catch (e: Exception) {
                // Log 5: Bắt được "thủ phạm" gây lỗi
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    suspend fun getMyPosts(context: Context): List<PostModel>? =
        withContext(Dispatchers.IO) {

            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/posts/my-posts")

                val request = Request.Builder()
                    .url("$BASE_URL/api/posts/my-posts")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        Log.e("API_DEBUG", " Lỗi Server: Code ${response.code} - ${response.message}")
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }


                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    val listPosts = json.decodeFromString<PostResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listPosts.size} bài viết")

                    return@withContext listPosts
                }
            } catch (e: Exception) {
                // Log 5: Bắt được "thủ phạm" gây lỗi
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    suspend fun getFollowedPosts(context: Context): List<PostModel>? =
        withContext(Dispatchers.IO) {
            // Log 1: Kiểm tra Token
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/posts/followed")

                val request = Request.Builder()
                    .url("$BASE_URL/api/posts/followed")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    // Log 2: Kiểm tra HTTP Code
                    if (!response.isSuccessful) {
                        Log.e("API_DEBUG", " Lỗi Server: Code ${response.code} - ${response.message}")
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }

                    // Log 3: In ra JSON thô mà Server trả về
                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    // Log 4: Thử Parse
                    val listPosts = json.decodeFromString<PostResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listPosts.size} bài viết")

                    return@withContext listPosts
                }
            } catch (e: Exception) {
                // Log 5: Bắt được "thủ phạm" gây lỗi
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
                null
            }
        }

    suspend fun getCommunityPosts(context: Context, id: Int): List<PostModel>? =
        withContext(Dispatchers.IO) {
            // Log 1: Kiểm tra Token
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/posts/community/${id}")

                val request = Request.Builder()
                    .url("$BASE_URL/api/posts/community/${id}")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    // Log 2: Kiểm tra HTTP Code
                    if (!response.isSuccessful) {
                        Log.e("API_DEBUG", " Lỗi Server: Code ${response.code} - ${response.message}")
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }

                    // Log 3: In ra JSON thô mà Server trả về
                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    // Log 4: Thử Parse
                    val listPosts = json.decodeFromString<PostResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listPosts.size} bài viết")

                    return@withContext listPosts
                }
            } catch (e: Exception) {
                // Log 5: Bắt được "thủ phạm" gây lỗi
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
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