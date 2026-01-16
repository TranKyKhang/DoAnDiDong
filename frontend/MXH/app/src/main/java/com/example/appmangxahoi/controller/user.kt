// --- UserController.kt ---
package com.example.appmangxahoi.controller

import UserModel
import android.content.Context
import android.net.Uri
import com.example.appmangxahoi.model.LoginRequest
import com.example.appmangxahoi.model.LoginResponse
import com.example.appmangxahoi.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class User {

    private val client = OkHttpClient()
    private val BASE_URL = "http://10.0.2.2:3000"

    private val json = Json { ignoreUnknownKeys = true }


    suspend fun getMyProfile(context: Context): UserModel? =
        withContext(Dispatchers.IO) {

            val token = TokenManager.getToken(context) ?: return@withContext null

            try {
                val request = Request.Builder()
                    .url("$BASE_URL/api/users/profile")
                    .addHeader("Authorization", "Bearer $token")
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
        displayName: String?,
        bio: String?,
        avatarUri: Uri?,
        bannerUri: Uri?
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            val token = TokenManager.getToken(context)
                ?: return@withContext false

            val bodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            displayName?.let {
                bodyBuilder.addFormDataPart("display_name", it)
            }

            bio?.let {
                bodyBuilder.addFormDataPart("bio", it)
            }

            avatarUri?.let { uri ->
                val file = uriToFile(context, uri)
                bodyBuilder.addFormDataPart(
                    "avatar",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
            }

            bannerUri?.let { uri ->
                val file = uriToFile(context, uri)
                bodyBuilder.addFormDataPart(
                    "banner",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
            }

            val request = Request.Builder()
                .url("$BASE_URL/api/users/profile-update")
                .put(bodyBuilder.build())
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

    // ================= helper =================
    fun uriToFile(context: Context, uri: Any): File {
        val realUri = uri as Uri

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(realUri) // image/png, image/jpeg

        val extension = when (mimeType) {
            "image/png" -> ".png"
            "image/jpeg" -> ".jpg"
            "image/jpg" -> ".jpg"
            "image/webp" -> ".webp"
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

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse? = withContext(Dispatchers.IO) {
        try {
            val loginRequest = LoginRequest(email, password)

            val requestBody = json.encodeToString(
                LoginRequest.serializer(),
                loginRequest
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/login")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null

                return@withContext json.decodeFromString(
                    LoginResponse.serializer(),
                    body
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

