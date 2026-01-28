package com.example.appmangxahoi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class CommunityResponse(
    val success: Boolean? = null,
    val data: List<CommunityModel>
)
@Serializable
data class DetailCommunityResponse(
    val message: String,
    val data: CommunityModel
)
@Serializable
data class CommunityModel(
    val id: Int,

    val name: String,

    val description: String? = null,

    val icon: String? = null,

    val banner: String? = null,

    val rules: String? = null,

    @SerialName("member_count")
    val memberCount: Int? = 0
)

@Serializable
data class MemberModel(
    val id: Int,
    val username: String,
    val displayName: String?,
    val avatar: String?,
    val role: String, // "admin", "moderator", "member",
    val isBanned: Int
)