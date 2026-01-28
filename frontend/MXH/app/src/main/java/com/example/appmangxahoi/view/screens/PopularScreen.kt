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
import kotlinx.coroutines.launch

@Composable
fun PopularScreen(
    topPadding: Dp = 0.dp,
    onPostClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val postController = remember { post() }

    // 🔹 State
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var isLastPage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // 🔹 Call API khi page thay đổi
    LaunchedEffect(page) {
        if (isLoading) return@LaunchedEffect
        isLoading = true

        val result = postController.getPopularPosts(context, page)

        if (result != null) {


            val newPosts = if (page == 1) {
                result
            } else {
                (posts + result)
                    .distinctBy { it.id }
            }

            posts = newPosts
            isLastPage = result.size < 20

            Log.d(
                "POPULAR_SCREEN",
                "Page $page - total ${posts.size} posts"
            )
        } else {
            Log.e("POPULAR_SCREEN", "API trả về null")
        }

        isLoading = false
    }
    fun reloadPosts() {
        scope.launch {
            isLoading = true
            val result = postController.getFollowedPosts(context, page)
            posts = result ?: emptyList()
            isLastPage = result?.size ?: 0 < 20
            isLoading = false
        }
    }

    // 🔹 Debug duplicate id (có thể xoá sau)
    LaunchedEffect(posts) {
        val dup = posts.groupBy { it.id }.filter { it.value.size > 1 }
        if (dup.isNotEmpty()) {
            Log.e("POPULAR_SCREEN", "DUPLICATE IDS: $dup")
        }
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

            // 🔹 Danh sách bài viết
            items(
                items = posts,
                key = { "${it.id}_${it.createdAt}" }
            ) { post ->
                AppPostItem(
                    post = post,
                    onClick = {
                        onPostClick(post.id.toString())
                    },
                    onPostUpdated ={
                        reloadPosts()
                    }
                )
            }

            // 🔹 Footer phân trang
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(
                        onClick = {
                            if (page > 1 && !isLoading) page--
                        },
                        enabled = page > 1 && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Trang trước"
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!isLastPage && !isLoading) page++
                        },
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

        // 🔹 Loading overlay
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
