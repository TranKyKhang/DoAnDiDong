package com.example.appmangxahoi.controller

import com.example.appmangxahoi.model.DCPost
import com.example.appmangxahoi.model.DataClassCommunities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class Communities{
    private val client = OkHttpClient()
    private val URL = "http://10.0.2.2:3000"
    suspend fun getDetail(id: Int): DataClassCommunities? =
        withContext(Dispatchers.IO){
            try {
                val request = Request.Builder()
                    .url("$URL/api/communities/details/$id")
                    .get()
                    .build()
                client.newCall(request).execute().use { resp ->
                    if(!resp.isSuccessful) return@withContext null
                    val body = resp.body?.string() ?: return@withContext null
                    val obj = JSONObject(body)
                    val data = obj.optJSONObject("data")
                    val json = Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                    val detail = json.decodeFromString<DataClassCommunities>(data.toString())
                    return@withContext detail
                }
            }catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}