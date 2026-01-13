package com.example.appmangxahoi.model

import kotlinx.serialization.Serializable

@Serializable
data class DataClassCommunities(
    val id: Int,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val banner: String? = null,
    val rules: String? = null,
    val member_count: Int,
    val created_at: String,
    val user_id: Int,
)
