// --- UserController.kt ---
package com.example.appmangxahoi.controller

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.appmangxahoi.model.LoginRequest
import com.example.appmangxahoi.model.LoginResponse
import com.example.appmangxahoi.model.RegisterRequest
import com.example.appmangxahoi.model.RegisterResponse
import com.example.appmangxahoi.model.UserModel
import com.example.appmangxahoi.utils.TokenManager
import com.example.appmangxahoi.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserController {

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

    suspend fun login(email: String, password: String, context: Context): LoginResponse? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                LoginRequest.serializer(),
                LoginRequest(email, password)
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/login")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val result = json.decodeFromString(LoginResponse.serializer(), body)

                // Lưu token ngay trong controller nếu thành công
                if (result.success && result.token != null) {
                    TokenManager.saveToken(context, result.token)
                    UserManager.saveLoginMethod(context, "email")
                }

                return@withContext result
            }
        } catch (e: Exception) {
            Log.e("UserController", "Login error: ${e.message}")
            null
        }
    }

    suspend fun googleLogin(idToken: String, context: Context): LoginResponse? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = """
            {
                "idToken": "$idToken"
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/auth/google-login")
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val result = json.decodeFromString(LoginResponse.serializer(), body)

                // Automatic token storage upon successful federated login for longitudinal user session tracking.
                if (result.success && result.token != null) {
                    TokenManager.saveToken(context, result.token)
                    UserManager.saveLoginMethod(context, "google")
                }

                return@withContext result
            }
        } catch (e: Exception) {
            Log.e("UserController", "Google login error: ${e.message}")
            null
        }
    }

    suspend fun register(email: String, username: String, password: String, context: Context): RegisterResponse? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                RegisterRequest.serializer(),
                RegisterRequest(email, username, password)
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/register")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext null
                val result = json.decodeFromString(RegisterResponse.serializer(), body)

                // Lưu token ngay trong controller nếu thành công
                if (result.success && result.token != null) {
                    TokenManager.saveToken(context, result.token)
                }

                return@withContext result
            }
        } catch (e: Exception) {
            Log.e("UserController", "Register error: ${e.message}")
            null
        }
    }

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        context: Context
    ): Boolean = withContext(Dispatchers.IO) {
        val token = TokenManager.getToken(context) ?: return@withContext false

        try {
            val jsonBody = """
            {
                "old_password": "$oldPassword",
                "new_password": "$newPassword"
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/change-password")
                .addHeader("Authorization", "Bearer $token")
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Optional: clear token if server requires re-login after password change
                    return@withContext true
                } else {
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun forgotPassword(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = """{"email": "$email"}""".toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/forgot-password")
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyResetOtp(email: String, otp: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = """
            {
                "email": "$email",
                "otp": "$otp",
                "newPassword": "$newPassword"
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/api/users/verify-reset-otp")
                .post(jsonBody)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

}

