package com.example.appmangxahoi.model

import kotlinx.serialization.Serializable

@Serializable
data class DataClassComment(
    val id: Int,
    val content: String,
    val created_at: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val rating: Int = 0,
    val is_removed: Int = 0,
    val depth_level: Int = 0,
    val user_id: Int,
    val post_id: Int,
    val parent_id: Int? = null,
    val username: String? = null,
    val user_vote_status: String? = null,
    val author_avatar: String? = null,
    val children: List<DataClassComment> = emptyList()

)
