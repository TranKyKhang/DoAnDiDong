package com.example.appmangxahoi.controller

import android.content.Context
import android.util.Log
import com.example.appmangxahoi.model.CommunityModel
import com.example.appmangxahoi.model.CommunityResponse
import com.example.appmangxahoi.model.DetailCommunityResponse
import com.example.appmangxahoi.model.PostResponse
import com.example.appmangxahoi.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

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

    }

