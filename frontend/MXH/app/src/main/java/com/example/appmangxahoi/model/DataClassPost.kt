package com.example.appmangxahoi.model

// Xóa class DataClassPost cũ đi, chúng ta chỉ dùng PostModel duy nhất
// Dữ liệu giả lập (Mock Data) bây giờ sẽ dùng PostModel chuẩn
val mockPosts = listOf(
    PostModel(
        id = 1,
        communityId = 101,
        communityName = "androiddev", // UI sẽ tự thêm prefix "r/"
        authorName = "dev_master",    // UI sẽ tự thêm prefix "u/"
        authorAvatarUrl = "/upload/avatars/user1.jpg", // Giả lập đường dẫn tương đối (hoặc để full link nếu muốn test)
        userId = 50,
        title = "Jetpack Compose quá tuyệt vời! Giao diện mượt mà.",
        content = "Tôi vừa chuyển từ XML sang Compose và cảm thấy năng suất tăng gấp đôi. Mọi người nghĩ sao?",
        // Convert imageUrl cũ thành List images
        images = listOf("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1000&auto=format&fit=crop"),
        video = null,
        upvotes = 1200, // Convert "1.2k" thành số Int
        downvotes = 50,
        rating = 1150,
        commentCount = 340,
        createdAt = "2h ago", // PostModel hứng String nên để tạm string này
        _isRemoved = 0,
        hotScore = 99.5
    ),
    PostModel(
        id = 2,
        communityId = 102,
        communityName = "funny",
        authorName = "joker",
        authorAvatarUrl = "/upload/avatars/user2.jpg",
        userId = 51,
        title = "Mèo của tôi đang học code (Thật đấy!)",
        content = "Nó cứ nhảy lên bàn phím mỗi khi tôi debug...",
        images = listOf("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=1000&auto=format&fit=crop"),
        video = null,
        upvotes = 5600,
        downvotes = 100,
        rating = 5500,
        commentCount = 890,
        createdAt = "5h ago",
        _isRemoved = 0,
        hotScore = 88.0
    ),
    PostModel(
        id = 3,
        communityId = 103,
        communityName = "kotlin",
        authorName = "jetbrains_fan",
        authorAvatarUrl = "/upload/avatars/user3.jpg",
        userId = 52,
        title = "Tại sao nên dùng Kotlin thay vì Java trong năm 2025?",
        content = "Null safety, Coroutines, và cú pháp ngắn gọn...",
        images = emptyList(), // Không có ảnh
        video = null,
        upvotes = 890,
        downvotes = 10,
        rating = 880,
        commentCount = 120,
        createdAt = "1d ago",
        _isRemoved = 0,
        hotScore = 45.0
    ),
    PostModel(
        id = 4,
        communityId = 104,
        communityName = "vietnam",
        authorName = "nguoiviet",
        authorAvatarUrl = "/upload/avatars/user4.jpg",
        userId = 53,
        title = "Ảnh chụp Sài Gòn về đêm tuyệt đẹp",
        content = "Một góc nhìn khác từ Bitexco...",
        images = listOf("https://images.unsplash.com/photo-1583417319070-4a69db38a482?q=80&w=1000&auto=format&fit=crop"),
        video = null,
        upvotes = 3400,
        downvotes = 200,
        rating = 3200,
        commentCount = 560,
        createdAt = "3h ago",
        _isRemoved = 0,
        hotScore = 70.0
    ),
    PostModel(
        id = 5,
        communityId = 105,
        communityName = "sports",
        authorName = "sport_lover",
        authorAvatarUrl = "/upload/avatars/user5.jpg",
        userId = 54,
        title = "[Highlight] Bàn thắng để đời của cầu thủ số 7!",
        content = "Xem đi xem lại vẫn thấy nổi da gà.",
        images = listOf("https://images.unsplash.com/photo-1579952363873-27f3bade8f55?q=80&w=1000&auto=format&fit=crop"), // Thumbnail cho video
        // Trường hợp Video
        video = "/videos/highlight.mp4", // Giả lập link video
        upvotes = 12000,
        downvotes = 500,
        rating = 11500,
        commentCount = 1500,
        createdAt = "10m ago",
        _isRemoved = 0,
        hotScore = 150.0
    )
)