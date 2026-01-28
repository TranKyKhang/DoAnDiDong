package com.example.appmangxahoi.model


import android.annotation.SuppressLint
import kotlinx.serialization.Serializable



@Serializable
data class DCPost(
    val id: Int,
    val title: String,

    val content: String? = null,
    val video: String? = null,
    val link_url: String? = null,

    val created_at: String? = null,

    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val rating: Int = 0,
    val comment_count: Int = 0,

    val is_removed: Int = 0,

    // ===== AUTHOR =====
    val author_id: Int,
    val author_name: String? = null,
    val author_display_name: String? = null,
    val author_avatar: String? = null,

    // ===== COMMUNITY =====
    val community_id: Int,
    val community_name: String? = null,
    val community_icon: String? = null,

    val images: List<DataClassPostImage> = emptyList(),
    val user_vote_status: String? = null
)


