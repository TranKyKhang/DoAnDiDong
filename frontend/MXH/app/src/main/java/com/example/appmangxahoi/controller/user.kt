// --- UserController.kt ---
package com.example.appmangxahoi.controller

import android.content.Context
import android.net.Uri
import com.example.appmangxahoi.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class User {

    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"

    private val json = Json { ignoreUnknownKeys = true }


    suspend fun getUserProfile(userId: Int): UserModel? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/users/$userId")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    return@withContext json.decodeFromString(UserModel.serializer(), body)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    suspend fun updateProfile(
        context: Context,
        userId: Int,
        displayName: String?,
        bio: String?,
        avatarUri: Uri?,
        bannerUri: Uri?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", userId.toString())

            displayName?.let { bodyBuilder.addFormDataPart("display_name", it) }
            bio?.let { bodyBuilder.addFormDataPart("bio", it) }

            avatarUri?.let {
                val file = uriToFile(context, it)
                bodyBuilder.addFormDataPart(
                    "avatar",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
            }

            bannerUri?.let {
                val file = uriToFile(context, it)
                bodyBuilder.addFormDataPart(
                    "banner",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
            }

            val request = Request.Builder()
                .url("$BASE_URL/api/users/update-profile")
                .put(bodyBuilder.build())
                .build()

            client.newCall(request).execute().use { response ->
                println("updateProfile response code: ${response.code}")
                return@withContext response.isSuccessful
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI: $uri")
        val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
