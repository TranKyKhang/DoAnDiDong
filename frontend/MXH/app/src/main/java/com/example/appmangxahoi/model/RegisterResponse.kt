package com.example.appmangxahoi.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserModel? = null
)