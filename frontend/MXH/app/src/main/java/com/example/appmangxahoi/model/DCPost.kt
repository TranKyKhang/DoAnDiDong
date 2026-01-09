package com.example.appmangxahoi.model


import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DCPost(
    val id: Int,
    val title: String,
    val content: String? = null, // Thêm ?
    val video: String? = null,   // QUAN TRỌNG: Thêm ? vì API trả về null
    val link_url: String? = null,
    val created_at: String,
    val upvotes: Int,
    val downvotes: Int,
    val rating: Int? = 0,        // Thêm ?
    val comment_count: Int,
    val is_removed: Int,         // QUAN TRỌNG: JSON trả về 0 (số), không phải Boolean
    val user_id: Int,
    val community_id: Int,
    val images: List<DataClassPostImage> = emptyList()
)


