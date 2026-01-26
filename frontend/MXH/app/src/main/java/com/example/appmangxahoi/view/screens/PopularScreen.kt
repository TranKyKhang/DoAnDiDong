package com.example.appmangxahoi.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.view.component.AppPostItem

@Composable
fun PopularScreen(
    topPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val postController = remember { post() }

    // 🔹 State
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var isLastPage by remember { mutableStateOf(false) }

    // 🔹 Gọi API mỗi khi page thay đổi
    LaunchedEffect(page,Unit) {
        isLoading = true

        val result = postController.getPopularPosts(context, page)

        if (result != null) {
            posts = result
            isLastPage = result.size < 20

            Log.d(
                "POPULAR_SCREEN",
                "Page $page - ${result.size} posts"
            )
        } else {
            Log.e("POPULAR_SCREEN", "API trả về null")
        }

        isLoading = false
    }

    // 🔹 UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = 16.dp
            )
        ) {

            // Danh sách bài viết
            items(
                items = posts,
                key = { it.id }
            ) { post ->
                AppPostItem(post = post)
            }

            // Footer phân trang
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // Trang trước
                    IconButton(
                        onClick = { page-- },
                        enabled = page > 1 && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Trang trước"
                        )
                    }

                    //  Trang sau
                    IconButton(
                        onClick = { page++ },
                        enabled = !isLastPage && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Trang sau"
                        )
                    }
                }
            }
        }

        // Loading giữa màn hình
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
