package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appmangxahoi.model.mockPosts
import com.example.appmangxahoi.view.component.AppPostItem

// --- MOCK DATA COMMENT (Dữ liệu giả cho bình luận) ---
data class Comment(val id: Int, val author: String, val content: String, val time: String)
val mockComments = listOf(
    Comment(1, "u/nguoi_qua_duong", "Bài viết rất hữu ích, cảm ơn bác!", "1h"),
    Comment(2, "u/fan_cung", "Hóng phần tiếp theo ạ.", "2h"),
    Comment(3, "u/dev_quen", "Code đoạn này clean quá.", "5h"),
    Comment(4, "u/anti_fan", "Cũng thường thôi, không ấn tượng lắm.", "1d")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit
) {
    // Tìm bài viết trong list mockPosts dựa vào ID
    val post = remember(postId) { mockPosts.find { it.id == postId } }
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post?.subreddit ?: "Chi tiết bài viết", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Thanh nhập bình luận
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Viết bình luận...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = { /* Xử lý gửi comment */ }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF0079D3))
                }
            }
        }
    ) { padding ->
        if (post != null) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0))
            ) {
                // 1. HIỂN THỊ NỘI DUNG BÀI VIẾT (Tái sử dụng AppPostItem)
                item {
                    // Truyền onClick = {} rỗng vì đang ở chi tiết rồi, không cần bấm nữa
                    AppPostItem(post = post)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 2. TIÊU ĐỀ PHẦN BÌNH LUẬN
                item {
                    Text(
                        text = "Bình luận",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // 3. DANH SÁCH BÌNH LUẬN
                items(mockComments) { comment ->
                    CommentItem(comment)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy bài viết!")
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar giả
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=${comment.author}&background=random",
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = comment.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "• ${comment.time}", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = comment.content, fontSize = 14.sp)
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}