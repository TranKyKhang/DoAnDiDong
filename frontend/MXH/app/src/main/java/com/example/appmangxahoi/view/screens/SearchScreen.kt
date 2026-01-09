package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmangxahoi.model.mockPosts // Đảm bảo import đúng
import com.example.appmangxahoi.view.component.AppPostItem

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
    targetCommunity: String? = null
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    // Xử lý tên hiển thị (nếu có targetCommunity thì đổi _ thành /)
    val targetDisplayName = remember(targetCommunity) {
        targetCommunity?.replace("_", "/")
    }

    // Logic lọc bài viết
    val filteredPosts = remember(query, targetDisplayName) {
        if (query.isBlank()) emptyList()
        else mockPosts.filter { post ->
            // Điều kiện 1: Phải khớp từ khóa
            val matchesQuery = post.title.contains(query, ignoreCase = true) ||
                    post.author.contains(query, ignoreCase = true) ||
                    post.subreddit.contains(query, ignoreCase = true)

            // Điều kiện 2: Nếu đang ở trong nhóm, bài viết phải thuộc nhóm đó
            val matchesCommunity = if (targetDisplayName != null) {
                post.subreddit == targetDisplayName
            } else {
                true // Nếu không có targetCommunity thì lấy hết
            }

            matchesQuery && matchesCommunity
        }
    }

    // Focus vào ô tìm kiếm khi mở màn hình
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background // Đảm bảo nền đồng bộ
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // --- QUAN TRỌNG: CHỈ LẤY PADDING BOTTOM, BỎ PADDING TOP Ở ĐÂY ---
                // Điều này giúp background tràn lên tận mép trên màn hình
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // --- 1. PHẦN THANH TÌM KIẾM ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // --- ĐẨY NỘI DUNG XUỐNG DƯỚI STATUS BAR TẠI ĐÂY ---
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                // TextField nhập liệu
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    // Hiển thị: "Tìm trong r/androiddev" hoặc "Tìm kiếm..."
                    placeholder = {
                        Text(if (targetDisplayName != null) "Tìm trong $targetDisplayName" else "Tìm kiếm...")
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Màu nền nhẹ cho ô tìm kiếm đẹp hơn
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium, // Bo tròn ô tìm kiếm
                    singleLine = true
                )

                // Nút xóa text
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }

            HorizontalDivider()

            // --- 2. DANH SÁCH KẾT QUẢ ---
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (query.isEmpty()) {
                    item {
                        Text(
                            "Tìm kiếm gần đây",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    val historyItems = listOf("Kotlin", "Android", "Vietnam")
                    items(historyItems) { historyItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = historyItem }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(historyItem)
                        }
                    }
                } else if (filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không tìm thấy kết quả nào", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredPosts) { post ->
                        AppPostItem(post = post)
                    }
                }
            }
        }
    }
}