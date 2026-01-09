package com.example.appmangxahoi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    @SerialName("display_name")
    val displayName: String? = null,

    val bio: String? = null,
    val avatar: String? = null,
    val banner: String? = null
)
