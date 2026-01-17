package com.example.appmangxahoi.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box // Dùng Box để xếp chồng loading lên trên
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.* // Import quan trọng: remember, mutableStateOf, LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.view.component.AppPostItem
// Import hàm gọi API của bạn (giả sử nó nằm ở file ApiService.kt hoặc Repository.kt)
// import com.example.appmangxahoi.api.getPopularPosts

@Composable
fun PopularScreen(
    topPadding: Dp = 0.dp
) {
    val context = LocalContext.current

    // 1. Khởi tạo Controller
    // Dùng remember để tạo instance của controller 1 lần duy nhất, KHÔNG ĐỂ NULL
    val postController = remember { post() }

    // 2. State dữ liệu
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 3. Gọi API
    LaunchedEffect(Unit) {
        val result = postController.getPopularPosts(context)

        if (result != null) {
            Log.d("DEBUG_APP", "Số lượng bài viết: ${result.size}")
            if (result.isNotEmpty()) {
                Log.d("DEBUG_APP", "Bài 1 title: ${result[0].title}")
                Log.d("DEBUG_APP", "Bài 1 images: ${result[0].images}")
            }
            posts = result
        } else {
            Log.e("DEBUG_APP", "Result bị null!")
        }
        isLoading = false
    }

    // 4. UI
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
            items(posts) { item -> // Đổi tên biến thành 'item' để tránh trùng tên với class 'post'
                AppPostItem(post = item)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // 3. UI hiển thị
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        // Danh sách bài viết
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = 16.dp
            )
        ) {
            items(posts) { post ->
                AppPostItem(post = post)
            }
        }

        // Hiển thị vòng xoay loading ở giữa màn hình nếu đang tải
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}