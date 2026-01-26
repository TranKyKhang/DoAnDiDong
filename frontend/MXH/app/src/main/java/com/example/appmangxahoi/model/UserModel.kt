package com.example.appmangxahoi.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserModel(
    val id: Int,
    val email: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val bio: String? = null,
    val avatar: String? = null,
    val banner: String? = null,
    @SerialName("post_rating")
    val postRating: Int = 0,
    @SerialName("comment_rating")
    val commentRating: Int = 0,
    @SerialName("login_method")
    val loginMethod: String? = "email"
)
