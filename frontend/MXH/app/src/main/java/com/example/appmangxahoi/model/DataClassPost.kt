package com.example.appmangxahoi.model

data class DataClassPost(
    val id: Int,
    val subreddit: String,
    val author: String,
    val authorAvatarUrl: String, // [MỚI] Link ảnh đại diện
    val title: String,
    val imageUrl: String?,
    val videoUrl: String? = null,
    val isVideo: Boolean = false,
    val voteCount: String,
    val commentCount: String,
    val timeAgo: String
)

val mockPosts = listOf(
    DataClassPost(
        id = 1, subreddit = "r/androiddev", author = "u/dev_master",
        authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=60", // Avatar nam
        title = "Jetpack Compose quá tuyệt vời! Giao diện mượt mà.",
        imageUrl = "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1000&auto=format&fit=crop",
        voteCount = "1.2k", commentCount = "340", timeAgo = "2h"
    ),
    DataClassPost(
        id = 2, subreddit = "r/funny", author = "u/joker",
        authorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&auto=format&fit=crop&q=60", // Avatar nữ
        title = "Mèo của tôi đang học code (Thật đấy!)",
        imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=1000&auto=format&fit=crop",
        voteCount = "5.6k", commentCount = "890", timeAgo = "5h"
    ),
    DataClassPost(
        id = 3, subreddit = "r/kotlin", author = "u/jetbrains_fan",
        authorAvatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&auto=format&fit=crop&q=60", // Avatar khác
        title = "Tại sao nên dùng Kotlin thay vì Java trong năm 2025?",
        imageUrl = null,
        voteCount = "890", commentCount = "120", timeAgo = "1d"
    ),
    DataClassPost(
        id = 4, subreddit = "r/vietnam", author = "u/nguoiviet",
        authorAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&auto=format&fit=crop&q=60", // Avatar nam kính
        title = "Ảnh chụp Sài Gòn về đêm tuyệt đẹp",
        imageUrl = "https://images.unsplash.com/photo-1583417319070-4a69db38a482?q=80&w=1000&auto=format&fit=crop",
        voteCount = "3.4k", commentCount = "560", timeAgo = "3h"
    ),
    DataClassPost(
        id = 5, subreddit = "r/sports", author = "u/sport_lover",
        authorAvatarUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100&auto=format&fit=crop&q=60", // Avatar thể thao
        title = "[Highlight] Bàn thắng để đời của cầu thủ số 7!",
        imageUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade8f55?q=80&w=1000&auto=format&fit=crop",
        isVideo = true,
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        voteCount = "12k", commentCount = "1.5k", timeAgo = "10m"
    )
)