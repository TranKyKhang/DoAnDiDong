package com.example.appmangxahoi.controller

import android.util.Log // Import Log
import com.example.appmangxahoi.model.CommunityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.io.use
import android.content.Context
import android.net.Uri
import com.example.appmangxahoi.view.screens.CreateCommunityData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CommunityController {

    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"

    // Thêm coercesInputValues = true để linh hoạt hơn khi parse JSON
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Serializable
    data class CommunityApiResponse(
        val data: List<CommunityModel>
    )

    suspend fun getCommunitiesByUser(userId: Int): List<CommunityModel> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("API_TEST", "Đang gọi API: $BASE_URL/api/communities/user/$userId")

                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/user/$userId")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("API_ERROR", "Lỗi Server: ${response.code}")
                        return@withContext emptyList()
                    }

                    val body = response.body?.string()
                    Log.d("API_res", "Response: $body") // In response xem server trả về gì

                    if (body == null) return@withContext emptyList()

                    val apiResponse = json.decodeFromString<CommunityApiResponse>(body)
                    return@withContext apiResponse.data
                }
            } catch (e: Exception) {
                // In lỗi chi tiết ra Logcat để sửa
                Log.e("API_CRASH", "Lỗi App: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    // HÀM MỚI: Tạo cộng đồng
    suspend fun createCommunity(context: Context, data: CreateCommunityData): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // 1. Chuẩn bị Multipart Builder
                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

                // 2. Thêm các trường Text
                multipartBuilder.addFormDataPart("name", data.name)
                multipartBuilder.addFormDataPart("description", data.description)
                multipartBuilder.addFormDataPart("rules", data.rules)
                multipartBuilder.addFormDataPart("user_id", "5") // TODO: Thay bằng ID user thật từ session

                // 3. Xử lý ảnh Icon (nếu có)
                if (data.iconUri != null) {
                    val file = uriToFile(context, data.iconUri, "icon_temp.jpg")
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        multipartBuilder.addFormDataPart("icon", file.name, requestFile)
                    }
                }

                // 4. Xử lý ảnh Banner (nếu có)
                if (data.bannerUri != null) {
                    val file = uriToFile(context, data.bannerUri, "banner_temp.jpg")
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        multipartBuilder.addFormDataPart("banner", file.name, requestFile)
                    }
                }

                // 5. Tạo Request
                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/create") // Endpoint Node.js
                    .post(multipartBuilder.build())
                    .build()

                // 6. Thực thi
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("API_UPLOAD", "Tạo thành công: ${response.body?.string()}")
                        return@withContext true
                    } else {
                        Log.e("API_UPLOAD", "Lỗi: ${response.code} - ${response.body?.string()}")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                Log.e("API_UPLOAD", "Lỗi Exception: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }

    // Hàm phụ trợ: Chuyển Uri thành File để upload
    private fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}