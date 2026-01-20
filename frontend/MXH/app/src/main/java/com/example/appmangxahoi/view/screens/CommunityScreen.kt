package com.example.appmangxahoi.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import com.example.appmangxahoi.model.MemberModel
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.view.component.AppPostItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityId: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current

    // Controller
    val postController = remember { post() }
    val communityController = remember { community() }
    val scope = rememberCoroutineScope()

    // State dữ liệu
    var community by remember { mutableStateOf<CommunityModel?>(null) }
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var memberList by remember { mutableStateOf<List<MemberModel>>(emptyList()) }

    // ===== PHÂN TRANG =====
    var page by remember { mutableStateOf(1) }
    var isLastPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }

    // State UI
    var isLoading by remember { mutableStateOf(true) }
    var isJoined by remember { mutableStateOf(false) }
    var isNotified by remember { mutableStateOf(false) }

    // BottomSheet
    var showMemberSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // ===== LOAD DATA =====
    LaunchedEffect(communityId, page) {
        if (page == 1) isLoading = true else isLoadingMore = true

        val newPosts = postController.getCommunityPosts(context, communityId, page) ?: emptyList()

        posts = if (page == 1) newPosts else posts + newPosts
        isLastPage = newPosts.size < 20

        if (community == null) {
            community = communityController.getCommunitybyId(context, communityId)
            isJoined = communityController.checkIsJoined(communityId, context)
            memberList = communityController.getMembersByCommunityID(context, communityId)
        }

        isLoading = false
        isLoadingMore = false
    }

    // ===== LOADING FULL =====
    if (isLoading && page == 1) {
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
                            model = community?.banner?.let { "http://10.0.2.2:3000$it" }
                                ?: "https://picsum.photos/800/400",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )

                        AsyncImage(
                            model = "http://10.0.2.2:3000${community?.icon}",
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
                                            imageVector = if (isNotified) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (isJoined) {
                                                if (communityController.leaveCommunity(context, communityId)) {
                                                    isJoined = false
                                                    Toast.makeText(context, "Đã rời nhóm", Toast.LENGTH_SHORT).show()
                                                    memberList = communityController.getMembersByCommunityID(context, communityId)
                                                }
                                            } else {
                                                if (communityController.joinCommunity(context, communityId)) {
                                                    isJoined = true
                                                    Toast.makeText(context, "Tham gia thành công!", Toast.LENGTH_SHORT).show()
                                                    memberList = communityController.getMembersByCommunityID(context, communityId)
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isJoined) Color.Gray.copy(alpha = 0.2f) else Color(0xFF0079D3),
                                        contentColor = if (isJoined) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (isJoined) "Đã tham gia" else "Tham gia")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(community?.description ?: "")
                    }
                }
            }

            // ===== POSTS =====
            items(posts) { post ->
                AppPostItem(post = post)
            }

            // ===== FOOTER PHÂN TRANG =====
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (page > 1) page-- },
                        enabled = page > 1 && !isLoadingMore
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trang trước")
                    }

                    if (isLoadingMore) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }

                    IconButton(
                        onClick = { if (!isLastPage) page++ },
                        enabled = !isLastPage && !isLoadingMore
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Trang sau")
                    }
                }
            }
        }
    }

    // ===== MEMBER SHEET (GIỮ NGUYÊN) =====
    if (showMemberSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMemberSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(memberList) { member ->
                    MemberItemRow(member)
                }
            }
        }
    }
}

@Composable
fun MemberItemRow(member: MemberModel) {
    ListItem(
        headlineContent = { Text(member.username) }
    )
}
