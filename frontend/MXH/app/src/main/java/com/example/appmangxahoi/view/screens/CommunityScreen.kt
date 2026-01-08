package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityName: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val displayName = remember(communityName) { communityName.replace("_", "/") }
    var isJoined by remember { mutableStateOf(false) }
    var isNotified by remember { mutableStateOf(false) }

    val communityPosts = remember(displayName) {
        mockPosts.filter { it.subreddit == displayName }.ifEmpty { mockPosts }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // --- 2. SỬA NÚT SEARCH ---
                    IconButton(onClick = onSearchClick) { // Gọi hàm điều hướng khi click
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }


                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0)),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
        ) {
            item {
                Column(modifier = Modifier.background(Color.White)) {
                    Box(modifier = Modifier.height(200.dp)) {
                        AsyncImage(
                            model = "https://picsum.photos/seed/${communityName}banner/800/400",
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color.Gray)
                        )
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=$communityName&background=random&size=200",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(80.dp)
                                .align(Alignment.BottomStart)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .background(Color.White)
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isJoined) {
                                    IconButton(onClick = { isNotified = !isNotified }) {
                                        Icon(
                                            imageVector = if (isNotified) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Button(
                                    onClick = { isJoined = !isJoined },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isJoined) Color.LightGray.copy(alpha = 0.5f) else Color(0xFF0079D3),
                                        contentColor = if (isJoined) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = if (isJoined) "Đã tham gia" else "Tham gia",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        Text(text = displayName, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1.2m thành viên", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("•", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4.5k đang online", fontSize = 12.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chào mừng bạn đến với cộng đồng $displayName. Đây là nơi chia sẻ kiến thức, hỏi đáp và thảo luận về mọi chủ đề liên quan!",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            items(communityPosts) { post ->
                AppPostItem(post = post)
            }
        }
    }
}