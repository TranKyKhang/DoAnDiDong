package com.example.appmangxahoi.model

import UserModel
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserModel? = null,
    val message: String? = null
)