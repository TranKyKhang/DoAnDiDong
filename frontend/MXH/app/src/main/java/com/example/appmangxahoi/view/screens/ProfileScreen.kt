package com.example.appmangxahoi.view.screens

import UserModel
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import com.example.appmangxahoi.view.component.AppPostItem
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(context: Context = LocalContext.current) {

    val userController = remember { User() }
    val postController = remember { post() }
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf<UserModel?>(null) }
    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    // 🔹 ADD: PAGINATION STATE
    var page by remember { mutableStateOf(1) }
    var isLastPage by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    /* ================= LOAD PROFILE + POSTS ================= */
    LaunchedEffect(page) { // 🔹 ADD: depend page
        loading = true

        profile = userController.getMyProfile(context)

        val result = postController.getMyPosts(context, page) // 🔹 ADD: page
        if (result != null) {
            posts = result
            isLastPage = result.size < 20
        }

        loading = false
    }

    /* ================= SYNC PROFILE DATA ================= */
    LaunchedEffect(profile) {
        profile?.let {
            name = it.displayName ?: "No name"
            bio = it.bio ?: "Chưa có tiểu sử"

            avatarUrl = it.avatar?.let { path ->
                "http://10.0.2.2:3000$path?t=${System.currentTimeMillis()}"
            } ?: ""

            coverUrl = it.banner?.let { path ->
                "http://10.0.2.2:3000$path?t=${System.currentTimeMillis()}"
            } ?: ""
        }
    }

    /* ================= EDIT DIALOG ================= */
    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            currentBio = bio,
            currentAvatar = avatarUrl,
            currentCover = coverUrl,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio, avatarUri, bannerUri ->
                scope.launch {
                    userController.updateProfile(
                        context = context,
                        displayName = newName,
                        bio = newBio,
                        avatarUri = avatarUri,
                        bannerUri = bannerUri
                    )
                    profile = userController.getMyProfile(context)
                    showEditDialog = false
                }
            }
        )
    }

    /* ================= LOADING ================= */
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /* ================= UI ================= */
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        item(span = { GridItemSpan(3) }) {
            Column {

                /* ===== COVER + AVATAR ===== */
                Box(modifier = Modifier.height(240.dp)) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                    )
                }

                /* ===== NAME + EDIT ===== */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("u/${profile?.username ?: ""}", color = Color.Gray)
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }

                /* ===== BIO ===== */
                Text(
                    text = bio,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))
                /* ===== POST & COMMENT RATING ===== */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // POST RATING
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = (profile?.postRating ?: 0).toString(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Post rating", fontWeight = FontWeight.Bold)
                        }
                    }

                    // COMMENT RATING
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = (profile?.commentRating ?: 0).toString(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Comment rating", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

            }
        }

        /* ===== POSTS ===== */
        items(
            items = posts,
            span = { GridItemSpan(3) }
        ) { post ->
            AppPostItem(post = post)
        }

        /* ===== PAGINATION FOOTER (ADD) ===== */
        item(span = { GridItemSpan(3) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // PREVIOUS
                IconButton(
                    enabled = page > 1 && !loading,
                    onClick = { page-- }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous"
                    )
                }



                // NEXT
                IconButton(
                    enabled = !isLastPage && !loading,
                    onClick = { page++ }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

/*  EDIT PROFILE DIALOG */

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentAvatar: Any?,
    currentCover: Any?,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var isAvatar by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { if (isAvatar) avatarUri = it else bannerUri = it }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {

                Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Tiểu sử") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = {
                    isAvatar = true
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Đổi avatar")
                }

                TextButton(onClick = {
                    isAvatar = false
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Đổi banner")
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Button(onClick = { onSave(name, bio, avatarUri, bannerUri) }) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

