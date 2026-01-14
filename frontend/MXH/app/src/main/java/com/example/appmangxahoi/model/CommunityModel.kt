package com.example.appmangxahoi.model

import kotlinx.serialization.Serializable

// Sửa các trường này khớp với JSON API trả về
@Serializable
data class CommunityModel(
    val id: Int,
    val name: String,
    val description: String?,
    val icon:String?,
    val banner:String?,
    val rules:String?,
    val member_count: Int,
    val created_at: String,
    val user_id:Int
)

@Serializable
data class JoinRequest(
    val user_id: Int,
    val community_id: Int
)