package com.example.appmangxahoi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val success: Boolean? = null,
    val data: List<PostModel>
)

@Serializable
data class CreatePostResponse(
    val success: Boolean,
    val data: CreatedPostModel
)

@Serializable
data class PostModel(
    val id: Int,
    val title: String,
    val content: String,
    // Video path (có thể null)
    val video: String? = null,
    @SerialName("link_url")
    val linkUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    val upvotes: Int,
    val downvotes: Int,
    val rating: Int,
    @SerialName("comment_count")
    val commentCount: Int,
    // Xử lý logic is_removed (0/1)
    @SerialName("is_removed")
    private val _isRemoved: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("community_id")
    val communityId: Int,
    val images: List<String> = emptyList(),
    @SerialName("community_name")
    val communityName: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("authorAvatarUrl")
    val authorAvatarUrl: String? = null,
    @SerialName("user_vote_status")
    val userVoteStatus: String? = null,
    @SerialName("hot_score")
    val hotScore: Double? = 0.0
) {
    val isRemoved: Boolean
        get() = _isRemoved == 1
}

@Serializable
data class CreatedPostModel(
    val id: Int,
    val title: String,
    val content: String,
    @SerialName("community_id")
    val communityId: Int? = null,
    @SerialName("user_id")
    val userId: Int,
    val video: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String? = null
)