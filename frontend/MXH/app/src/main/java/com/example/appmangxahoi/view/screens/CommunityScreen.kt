package com.example.appmangxahoi.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ripple
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.community
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.CommunityModel
import com.example.appmangxahoi.model.MemberModel
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.utils.UserManager
import com.example.appmangxahoi.view.component.AppPostItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityId: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val postController = remember { post() }
    val communityController = remember { community() }
    val scope = rememberCoroutineScope()
    var community by remember { mutableStateOf<CommunityModel?>(null) }
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var memberList by remember { mutableStateOf<List<MemberModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var isJoined by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(1) }
    var isLastPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val myUserId = UserManager.getUserId(context)
    val myMember = memberList.find { it.id == myUserId }
    val isAmIBanned = myMember?.isBanned == 1
    LaunchedEffect(communityId, page, selectedTab) {
        if (page == 1) isLoading = true else isLoadingMore = true
        val result = when (selectedTab) {
            0 -> postController.getCommunityPosts(context, communityId, page)
            1 -> postController.getNewestCommunityPosts(context, communityId, page)
            2 -> postController.getRisingCommunityPosts(context, communityId, page)
            else -> emptyList()
        }
        if (page == 1) {
            posts = result ?: emptyList()
        } else {
            posts = posts + (result ?: emptyList())
        }
        isLastPage = (result?.size ?: 0) < 20
        if (page == 1) {
            community = communityController.getCommunitybyId(context, communityId)
            isJoined = communityController.checkIsJoined(communityId, context)
            memberList = communityController.getMembersByCommunityID(context, communityId)
            isLoading = false
        }
        isLoadingMore = false
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
                        if (!isAmIBanned) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .background(Color.Black.copy(0.4f), CircleShape)
                                    .padding(8.dp)
                            )
                        }
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
                    val bannerInteractionSource = remember { MutableInteractionSource() }
                    Box(modifier = Modifier.height(200.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clickable(
                                    interactionSource = bannerInteractionSource,
                                    indication = ripple(),
                                    onClick = {} // Nếu có onClick cho banner
                                )
                        ) {
                            AsyncImage(
                                model = community?.banner?.let { "http://10.0.2.2:3000$it" }
                                    ?: "https://picsum.photos/800/400",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = community?.name ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if(!isAmIBanned){
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val success =
                                                if (!isJoined)
                                                    communityController.joinCommunity(context, communityId)
                                                else
                                                    communityController.leaveCommunity(context, communityId)
                                            if (success) {
                                                isJoined = !isJoined
                                                memberList =
                                                    communityController.getMembersByCommunityID(context, communityId)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(if (isJoined) "Đã tham gia" else "Tham gia")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if(isAmIBanned){
                            Text(
                                text = "Bạn đã bị cấm khỏi nhóm này",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        else{
                            Text(community?.description ?: "")
                            TabRow(selectedTabIndex = selectedTab) {
                                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                    Text("Hot")
                                }
                                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                    Text("Mới nhất")
                                }
                                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                    Text("Đang lên")
                                }
                                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                                    Text("Thành viên (${memberList.size})")
                                }
                            }
                        }
                    }
                }
            }
            if (selectedTab == 0 || selectedTab == 1 || selectedTab == 2 && !isAmIBanned) {
                items(posts) { post ->
                    AppPostItem(
                        post = post,
                        onClick = {
                            onPostClick(post.id.toString())

                        },
                        onPostUpdated = {
                            reloadPosts()
                        }
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val prevInteractionSource = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = { if (page > 1) page-- },
                            enabled = page > 1 && !isLoadingMore,
                            interactionSource = prevInteractionSource
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Trang trước"
                            )
                        }
                        val nextInteractionSource = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = { if (!isLastPage) page++ },
                            enabled = !isLastPage && !isLoadingMore,
                            interactionSource = nextInteractionSource
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Trang sau"
                            )
                        }
                    }
                }
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            if (selectedTab == 3 && myMember != null && !isAmIBanned) {
                items(memberList) { member ->
                    MemberItemRow(
                        member = member,
                        currentUserId = myMember.id,
                        currentUserRole = myMember.role,
                        onChangeRole = { memberId, newRole ->
                            scope.launch {
                                val success = communityController.changeMemberRole(
                                    context,
                                    communityId,
                                    memberId,
                                    newRole
                                )
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Đã đổi role",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    memberList =
                                        communityController.getMembersByCommunityID(context, communityId)
                                }
                            }
                        },
                        onBanClick = { memberId, isBannedNow ->
                            scope.launch {
                                val newStatus = if (isBannedNow == 1) 0 else 1
                                val success = if (newStatus == 1) {
                                    communityController.banUser(context, communityId, memberId)
                                } else {
                                    communityController.unBanUser(context, communityId, memberId)
                                }
                                if (success) {
                                    memberList = memberList.map { member ->
                                        if (member.id == memberId) {
                                            member.copy(isBanned = newStatus)
                                        } else {
                                            member
                                        }
                                    }
                                    val msg = if (newStatus == 1) "Đã cấm thành công" else "Đã gỡ cấm"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberItemRow(
    member: MemberModel,
    currentUserId: Int,
    currentUserRole: String,
    onChangeRole: (Int, String) -> Unit,
    onBanClick: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(member.role) }
    var showBanDialog by remember { mutableStateOf(false) }
    val isBanned = member.isBanned == 1
    val canInteract = when {
        currentUserId == member.id -> false
        currentUserRole == "admin" -> true
        currentUserRole == "moderator" && member.role == "member" -> true
        else -> false
    }
    val roles = when {
        currentUserRole == "admin" && member.role == "member" -> listOf("member", "moderator")
        currentUserRole == "admin" && member.role == "moderator" -> listOf("moderator", "member")
        currentUserRole == "moderator" && member.role == "member" -> listOf("member", "moderator")
        else -> listOf(member.role)
    }
    if (showBanDialog) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = { Text(if (isBanned) "Gỡ cấm (Unban)" else "Cấm thành viên (Ban)") },
            text = { Text("Bạn có chắc chắn muốn thực hiện với ${member.displayName}?") },
            confirmButton = {
                TextButton(onClick = {
                    showBanDialog = false
                    onBanClick(member.id, member.isBanned)
                }) {
                    Text("Đồng ý", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) { Text("Hủy") }
            }
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    ListItem(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    if (!canInteract) {
                        Toast.makeText(context, "Không có quyền thao tác", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            .alpha(if (isBanned) 0.5f else 1f),
        leadingContent = {
            AsyncImage(
                model = member.avatar?.let { "http://10.0.2.2:3000$it" },
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
        },
        headlineContent = {
            Text(
                text = member.displayName ?: member.username,
                textDecoration = if (isBanned) TextDecoration.LineThrough else null,
                color = if (isBanned) Color.Red else Color.Unspecified
            )
        },
        supportingContent = {
            if (!canInteract) {
                Text(text = member.role, color = Color.Gray)
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    TextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    expanded = false
                                    if (role != selectedRole) {
                                        selectedRole = role
                                        onChangeRole(member.id, role)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            if (canInteract) {
                IconButton(onClick = { showBanDialog = true }) {
                    if (isBanned) {
                        Icon(Icons.Default.Refresh, contentDescription = "Unban", tint = Color.Blue)
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = "Ban", tint = Color.Red)
                    }
                }
            }
        }
    )
}