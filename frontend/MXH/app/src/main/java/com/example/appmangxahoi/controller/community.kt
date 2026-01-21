package com.example.appmangxahoi.controller

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.appmangxahoi.model.CommunityModel
import com.example.appmangxahoi.model.CommunityResponse
import com.example.appmangxahoi.model.DetailCommunityResponse
import com.example.appmangxahoi.model.MemberModel
import com.example.appmangxahoi.model.PostResponse
import com.example.appmangxahoi.utils.TokenManager
import com.example.appmangxahoi.view.screens.CreateCommunityData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class community {
    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"

    private val json = Json { ignoreUnknownKeys = true }
    suspend fun getMyCommunity(context: Context): List<CommunityModel>?=
        withContext(Dispatchers.IO) {

            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/communities/user")

                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/user")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        Log.e(
                            "API_DEBUG",
                            " Lỗi Server: Code ${response.code} - ${response.message}"
                        )
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }


                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    val listCommunity = json.decodeFromString<CommunityResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listCommunity.size} bài viết")

                    return@withContext listCommunity
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
                null
            }
        }

    suspend fun getCommunitybyId(context: Context, id: Int): CommunityModel?=
        withContext(Dispatchers.IO) {

            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext null
            }

            try {
                Log.d("API_DEBUG", " Bắt đầu gọi API: $BASE_URL/api/communities/details/${id}")

                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/details/${id}")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        Log.e(
                            "API_DEBUG",
                            " Lỗi Server: Code ${response.code} - ${response.message}"
                        )
                        return@withContext null
                    }

                    val body = response.body?.string()
                    if (body == null) {
                        Log.e("API_DEBUG", " Lỗi: Body rỗng")
                        return@withContext null
                    }


                    Log.d("API_DEBUG", " Server trả về JSON: $body")

                    val listCommunity = json.decodeFromString<DetailCommunityResponse>(body).data
                    Log.d("API_DEBUG", " Parse thành công: ${listCommunity} bài viết")

                    return@withContext listCommunity
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", " CRASH KHI GỌI API/PARSE JSON: ${e.message}")
                e.printStackTrace()
                null
            }
        }

    //Tao cong dong
    suspend fun createCommunity(
        context: Context,
        data: CreateCommunityData, // Chứa name, description, rules, uri ảnh
    ): Boolean = withContext(Dispatchers.IO) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
            return@withContext false
        }
        try {
            // Tạo Multipart Builder
            val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

            multipartBuilder.addFormDataPart("name", data.name)
            multipartBuilder.addFormDataPart("description", data.description)
            multipartBuilder.addFormDataPart("rules", data.rules)

            // Xử lý ảnh Icon (Khớp với req.files['icon'])
            data.iconUri?.let { uri ->
                val file = uriToFile(context, uri, "icon_temp.jpg")
                file?.let {
                    // "icon" là key mà backend đang chờ trong req.files['icon']
                    val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                    multipartBuilder.addFormDataPart("icon", it.name, requestBody)
                }
            }

            // Xử lý ảnh Banner (Khớp với req.files['banner'])
            data.bannerUri?.let { uri ->
                val file = uriToFile(context, uri, "banner_temp.jpg")
                file?.let {
                    // "banner" là key mà backend đang chờ trong req.files['banner']
                    val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                    multipartBuilder.addFormDataPart("banner", it.name, requestBody)
                }
            }

            // Tạo Request với Header Token
            val request = Request.Builder()
                .url("$BASE_URL/api/communities/create")
                .addHeader("Authorization", "Bearer $token") // Server dùng cái này để ra req.user.id
                .post(multipartBuilder.build())
                .build()

            // Gửi và xử lý kết quả
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Backend trả về 201 Created
                    Log.d("API_UPLOAD", "Tạo thành công: ${response.body?.string()}")
                    return@withContext true
                } else {
                    // Backend trả về 400, 409 hoặc 500
                    Log.e("API_UPLOAD", "Lỗi Server: ${response.code} - ${response.body?.string()}")
                    return@withContext false
                }
            }

        } catch (e: Exception) {
            Log.e("API_UPLOAD", "Lỗi App: ${e.message}")
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
    //Kiem tra tham gia hay chua
    suspend fun checkIsJoined(communityId: Int,context: Context): Boolean =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext false
            }
            try {
                // Tạo JSON chỉ chứa community_id
                val jsonObject = JSONObject()
                jsonObject.put("community_id", communityId)

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)

                // Gửi Request kèm Token
                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/check_status")
                    .addHeader("Authorization", "Bearer $token") // Server tự lấy userId từ đây
                    .post(requestBody)
                    .build()

                // Xử lý kết quả
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val jsonResponse = JSONObject(body)
                            // Server trả về: { "isJoined": true } hoặc false
                            return@withContext jsonResponse.optBoolean("isJoined", false)
                        }
                    }
                    return@withContext false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }

    suspend fun joinCommunity(context: Context,communityId: Int): Boolean =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext false
            }
            try {
                // Tạo JSON Body
                val jsonObject = JSONObject()
                jsonObject.put("community_id", communityId)

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)

                // Tạo Request kèm Token
                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/join")
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                // Gửi Request
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("API_JOIN", "Tham gia thành công!")
                        return@withContext true
                    } else {
                        // Backend có thể trả về 409 (đã tham gia) hoặc 400/500
                        Log.e("API_JOIN", "Lỗi: ${response.code} - ${response.body?.string()}")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                Log.e("API_JOIN", "Lỗi kết nối: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }

    suspend fun leaveCommunity(context: Context,communityId: Int): Boolean =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) {
                Log.e("API_DEBUG", " Lỗi: Token bị null (Chưa đăng nhập?)")
                return@withContext false
            }
            try {
                // Tạo JSON Body
                val jsonObject = JSONObject()
                jsonObject.put("community_id", communityId)

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)

                // Tạo Request kèm Token
                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/leave")
                    .addHeader("Authorization", "Bearer $token") // <--- Server dùng cái này để biết là ai
                    .post(requestBody)
                    .build()

                // Gửi Request
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("API_LEAVE", "Rời thành công!")
                        return@withContext true
                    } else {
                        // Backend có thể trả về 409 (đã tham gia) hoặc 400/500
                        Log.e("API_LEAVE", "Lỗi: ${response.code} - ${response.body?.string()}")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                Log.e("API_LEAVE", "Lỗi kết nối: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }

    //Lay danh sach thanh vien theo id
    suspend fun getMembersByCommunityID(context: Context, communityId: Int): List<MemberModel> =
        withContext(Dispatchers.IO) {
            val token = TokenManager.getToken(context)
            if (token == null) return@withContext emptyList()

            try {
                // 1. Tạo Request (Dùng GET, không cần Body)
                val request = Request.Builder()
                    .url("$BASE_URL/api/communities/$communityId/members")
                    .addHeader("Authorization", "Bearer $token")
                    .get() // <--- QUAN TRỌNG: Dùng GET
                    .build()

                // 2. Gọi lệnh
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()

                        // 3. Parse JSON
                        val jsonObject = JSONObject(responseBody ?: "")
                        val dataArray = jsonObject.getJSONArray("data")

                        val members = mutableListOf<MemberModel>()

                        for (i in 0 until dataArray.length()) {
                            val item = dataArray.getJSONObject(i)
                            members.add(
                                MemberModel(
                                    id = item.getInt("id"),
                                    username = item.getString("username"),
                                    displayName = if (item.has("display_name") && !item.isNull("display_name")) item.getString("display_name") else item.getString("username"),
                                    avatar = if (item.has("avatar") && !item.isNull("avatar")) item.getString("avatar") else null,
                                    role = item.getString("role")
                                )
                            )
                        }

                        Log.d("API_GetMembers", "Lấy được ${members.size} thành viên")
                        return@withContext members
                    } else {
                        Log.e("API_GetMembers", "Lỗi: ${response.code}")
                        return@withContext emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_GetMembers", "Exception: ${e.message}")
                return@withContext emptyList()
            }
        }

    suspend fun changeMemberRole(
        context: Context,
        communityId: Int,
        targetUserId: Int,
        role: String
    ): Boolean = withContext(Dispatchers.IO) {

        val token = TokenManager.getToken(context)
        if (token == null) {
            Log.e("API_CHANGE_ROLE", "Token null")
            return@withContext false
        }

        try {
            // Body gửi đúng với backend
            val jsonObject = JSONObject()
            jsonObject.put("targetUserId", targetUserId)
            jsonObject.put("role", role)

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL/api/communities/$communityId/change-role")
                .addHeader("Authorization", "Bearer $token")
                .put(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    Log.d("API_CHANGE_ROLE", "Đổi role thành công: $responseBody")
                    return@withContext true
                } else {
                    Log.e(
                        "API_CHANGE_ROLE",
                        "Lỗi ${response.code}: $responseBody"
                    )
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.e("API_CHANGE_ROLE", "Exception: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }


}

