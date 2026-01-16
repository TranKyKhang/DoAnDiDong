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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.community
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.CommunityModel
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.model.mockPosts
import com.example.appmangxahoi.view.component.AppPostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityId: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val postController = remember { post() }
    val communityController = remember { community() }

    var community by remember { mutableStateOf<CommunityModel?>(null) }
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var isJoined by remember { mutableStateOf(false) }
    var isNotified by remember { mutableStateOf(false) }

    // ===== LOAD DATA =====
    LaunchedEffect(communityId) {
        posts = postController.getCommunityPosts(context, communityId) ?: emptyList()
        community = communityController.getCommunitybyId(context, communityId)
        isLoading = false
    }

    // ===== LOADING =====
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .padding(8.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .padding(8.dp)
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

            // ===== HEADER =====
            item {
                Column(modifier = Modifier.background(Color.White)) {

                    Box(modifier = Modifier.height(200.dp)) {
                        AsyncImage(
                            model = community?.banner?.let {
                                "http://10.0.2.2:3000$it"
                            } ?: "https://picsum.photos/800/400",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )

                        AsyncImage(
                            model = "http://10.0.2.2:3000${community?.icon}"
                                ?: "https://ui-avatars.com/api/?name=${community?.name}",
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(80.dp)
                                .align(Alignment.BottomStart)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = community?.name ?: "Community",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                if (isJoined) {
                                    IconButton(onClick = { isNotified = !isNotified }) {
                                        Icon(
                                            if (isNotified)
                                                Icons.Filled.Notifications
                                            else
                                                Icons.Filled.NotificationsNone,
                                            contentDescription = null
                                        )
                                    }
                                }

                                Button(
                                    onClick = { isJoined = !isJoined },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (isJoined) "Đã tham gia" else "Tham gia")
                                }
                            }
                        }

                        // ===== SLUG =====
                        Text(
                            text = "r/${community?.name ?: ""}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ===== DESCRIPTION =====
                        Text(
                            text = community?.description
                                ?: "Chào mừng bạn đến với cộng đồng!",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // ===== POSTS =====
            items(posts) { post ->
                AppPostItem(post = post)
            }
        }
    }
}
