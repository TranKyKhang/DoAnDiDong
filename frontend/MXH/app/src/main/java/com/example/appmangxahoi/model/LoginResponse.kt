package com.example.appmangxahoi.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserModel? = null,
    val message: String? = null
)