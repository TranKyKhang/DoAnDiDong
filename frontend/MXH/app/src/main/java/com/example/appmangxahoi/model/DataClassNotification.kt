package com.example.appmangxahoi.model

import kotlinx.serialization.Serializable

@Serializable
data class DataClassNotification(
    val id: Int,
    val type: String,
    val content: String,
    val is_read: Int,
    val created_at: String,
    val recipient_id: Int,
    val sender_id: Int? = null,
    val post_id: Int? = null,
    val comment_id: Int? = null,
)

