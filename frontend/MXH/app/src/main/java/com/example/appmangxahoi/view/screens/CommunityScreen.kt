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
import androidx.compose.material.icons.filled.ArrowForward
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
    onSearchClick: () -> Unit
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

    // === PAGINATION ===
    var page by remember { mutableStateOf(1) }
    var isLastPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val myUserId = UserManager.getUserId(context)
    val myMember = memberList.find { it.id == myUserId }

    // === LOAD DATA ===
    LaunchedEffect(communityId, page) {

        if (page == 1) isLoading = true else isLoadingMore = true

        val result = postController.getCommunityPosts(
            context = context,
            id = communityId,
            page = page
        )

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
                            Icons.Default.Search,
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

            item {
                Column(modifier = Modifier.background(Color.White)) {

                    Box(modifier = Modifier.height(200.dp)) {
                        AsyncImage(
                            model = community?.banner?.let { "http://10.0.2.2:3000$it" }
                                ?: "https://picsum.photos/800/400",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
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

                        Spacer(Modifier.height(8.dp))
                        Text(community?.description ?: "")

                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                Text("Bài viết")
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("Thành viên (${memberList.size})")
                            }
                        }
                    }
                }
            }

            // ===== POSTS =====
            if (selectedTab == 0) {
                items(posts) { post ->
                    AppPostItem(post = post)
                }

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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Trang trước"
                            )
                        }

                        IconButton(
                            onClick = { if (!isLastPage) page++ },
                            enabled = !isLastPage && !isLoadingMore
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

            // ===== MEMBERS =====
            if (selectedTab == 1) {
                items(memberList) { member ->
                    MemberItemRow(
                        member = member,
                        currentUserId = myMember!!.id,
                        currentUserRole = myMember!!.role,
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
                        }
                    )
                }
            }
        }
    }
}

/* ====================== MEMBER ITEM ====================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberItemRow(
    member: MemberModel,
    currentUserId: Int,
    currentUserRole: String,
    onChangeRole: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(member.role) }

    val canInteract = when {
        currentUserId == member.id -> false
        currentUserRole == "admin" -> true
        currentUserRole == "moderator" && member.role == "member" -> true
        else -> false
    }

    val roles = when {
        currentUserRole == "admin" && member.role == "member" ->
            listOf("member", "moderator")

        currentUserRole == "admin" && member.role == "moderator" ->
            listOf("moderator", "member")

        currentUserRole == "moderator" && member.role == "member" ->
            listOf("member", "moderator")

        else -> listOf(member.role)
    }

    ListItem(
        modifier = Modifier.clickable {
            if (!canInteract) {
                Toast.makeText(
                    context,
                    "Bạn không có quyền thay đổi role",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        leadingContent = {
            AsyncImage(
                model = member.avatar?.let { "http://10.0.2.2:3000$it" },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        },
        headlineContent = {
            Text(member.displayName ?: member.username)
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
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
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
        }
    )
}
