package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmangxahoi.model.mockPosts
import com.example.appmangxahoi.view.component.AppPostItem

@Composable
fun PopularScreen(
    topPadding: Dp = 0.dp, // Nhận padding để tránh TopBar
    onPostClick: (Int) -> Unit
) {
    // Giả lập: Lấy các bài viết có subreddit là "r/funny" hoặc đảo ngẫu nhiên để làm "Popular"
    val popularPosts = mockPosts.shuffled()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)),
        contentPadding = PaddingValues(
            top = topPadding,
            bottom = 16.dp
        )
    ) {
        items(popularPosts) { post ->
            AppPostItem(
                post = post,
                onItemClick = { onPostClick(post.id) } // <--- Gọi callback khi click
            )
        }
    }
}