package com.example.appmangxahoi.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)