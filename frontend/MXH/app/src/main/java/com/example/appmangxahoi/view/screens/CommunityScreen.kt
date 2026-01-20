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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import com.example.appmangxahoi.model.MemberModel // Đảm bảo bạn đã tạo file MemberModel
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.utils.TokenManager
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

    // State Dữ liệu
    var community by remember { mutableStateOf<CommunityModel?>(null) }
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var memberList by remember { mutableStateOf<List<MemberModel>>(emptyList()) } // Danh sách thành viên thật

    // State UI
    var isLoading by remember { mutableStateOf(true) }
    var isJoined by remember { mutableStateOf(false) }
    var isNotified by remember { mutableStateOf(false) }

    // BottomSheet State
    var showMemberSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // ===== LOAD DATA TỪ API =====
    LaunchedEffect(communityId) {
        isLoading = true
        // Lấy bài viết
        posts = postController.getCommunityPosts(context, communityId) ?: emptyList()
        // Lấy thông tin nhóm
        community = communityController.getCommunitybyId(context, communityId)
        // Kiểm tra đã tham gia chưa
        isJoined = communityController.checkIsJoined(communityId, context)

        // LẤY DANH SÁCH THÀNH VIÊN
        memberList = communityController.getMembersByCommunityID(context, communityId)

        isLoading = false
    }

    // ===== MÀN HÌNH LOADING =====
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

            // ===== HEADER THÔNG TIN NHÓM =====
            item {
                Column(modifier = Modifier.background(Color.White)) {

                    // Banner + Avatar
                    Box(modifier = Modifier.height(200.dp)) {
                        AsyncImage(
                            model = community?.banner?.let { "http://10.0.2.2:3000$it" }
                                ?: "https://picsum.photos/800/400",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp)
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

                    // Tên + Nút Tham gia
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
                                                val success = communityController.leaveCommunity(context, communityId)
                                                if (success) {
                                                    isJoined = false
                                                    Toast.makeText(context, "Đã rời nhóm", Toast.LENGTH_SHORT).show()
                                                    // Reload lại danh sách thành viên
                                                    memberList = communityController.getMembersByCommunityID(context, communityId)
                                                }
                                            } else {
                                                val success = communityController.joinCommunity(context, communityId)
                                                if (success) {
                                                    isJoined = true
                                                    Toast.makeText(context, "Tham gia thành công!", Toast.LENGTH_SHORT).show()
                                                    // Reload lại danh sách thành viên
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

                        Text("r/${community?.name ?: ""}", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = community?.description ?: "Chào mừng bạn đến với cộng đồng!",
                            fontSize = 14.sp, color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                        // --- NÚT XEM THÀNH VIÊN ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMemberSheet = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Group, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            // Hiển thị số lượng thành viên thật
                            Text(
                                "Xem tất cả thành viên (${memberList.size})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }

            // ===== DANH SÁCH BÀI VIẾT =====
            items(posts) { post ->
                AppPostItem(post = post)
            }
        }
    }

    // ===== BOTTOM SHEET THÀNH VIÊN =====
    if (showMemberSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMemberSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Thành viên (${memberList.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn {
                    items(memberList) { member ->
                        MemberItemRow(member = member) // Tách UI ra hàm riêng cho gọn
                    }
                }
            }
        }
    }
}

// ===== UI TỪNG DÒNG THÀNH VIÊN (Tách ra cho code chính đỡ rối) =====
@Composable
fun MemberItemRow(member: MemberModel) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.White),
        leadingContent = {
            AsyncImage(
                model = if (!member.avatar.isNullOrEmpty()) "http://10.0.2.2:3000${member.avatar}"
                else "https://ui-avatars.com/api/?name=${member.username}",
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        },
        headlineContent = {
            Text(member.displayName ?: member.username, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            // Chỉ hiện role nếu không phải member thường
            if (member.role == "admin" || member.role == "moderator") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF0079D3)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.role.replaceFirstChar { it.uppercase() },
                        color = Color(0xFF0079D3),
                        fontSize = 12.sp
                    )
                }
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Tùy chọn")
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Xem trang cá nhân") },
                        onClick = { isMenuExpanded = false }
                    )
                    // Logic Admin/Mod quản lý thành viên có thể thêm ở đây sau
                }
            }
        }
    )
}