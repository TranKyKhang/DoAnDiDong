package com.example.appmangxahoi.view.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.model.UserModel
import com.example.appmangxahoi.model.mockPosts // Đảm bảo đã import mockPosts
import com.example.appmangxahoi.view.component.AppPostItem // Đảm bảo đã import AppPostItem
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(context: Context, userId: Int) {
    val userController = remember { User() }
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf<UserModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Load profile từ API
    LaunchedEffect(userId) {
        profile = userController.getUserProfile(userId)
    }

    // State hiển thị UI
    var name by remember { mutableStateOf("Otis Dev") }
    var bio by remember { mutableStateOf("Lập trình viên Mobile | Yêu thích Kotlin & Android") } // [MỚI] State Bio
    var avatarUri by remember { mutableStateOf<Any>("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200") }
    var coverUri by remember { mutableStateOf<Any>("https://picsum.photos/800/400") }

    // Cập nhật UI khi có dữ liệu từ API
    LaunchedEffect(profile) {
        profile?.let {
            name = it.displayName ?: "No name"
            // Giả sử API trả về bio, nếu UserModel chưa có trường bio thì bạn cần thêm vào model
            // bio = it.bio ?: "Chưa có tiểu sử" 
            avatarUri = it.avatar?.let { path -> "http://10.0.2.2:3000$path" } ?: ""
            coverUri = it.banner?.let { path -> "http://10.0.2.2:3000$path" } ?: ""
        }
    }

    // Dialog chỉnh sửa
    if (showEditDialog) {
        EditProfileDialog(
            context = context,
            currentName = name,
            currentBio = bio, // Truyền Bio hiện tại
            currentAvatar = avatarUri,
            currentCover = coverUri,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio, newAvatar, newCover ->
                // Gọi API Update
                scope.launch {
                    val success = userController.updateProfile(
                        context,
                        userId,
                        newName,
                        newBio, // [QUAN TRỌNG] Truyền bio vào vị trí tham số thứ 4 (trước đó là null)
                        newAvatar,
                        newCover
                    )
                    if (success) {
                        // Update UI nếu thành công
                        name = newName
                        bio = newBio
                        if (newAvatar != null) avatarUri = newAvatar
                        if (newCover != null) coverUri = newCover
                    }
                    showEditDialog = false
                }
            }
        )
    }

    // UI chính: Dùng LazyColumn thay vì LazyVerticalGrid
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F8)) // Màu nền xám nhẹ
    ) {
        // --- PHẦN HEADER PROFILE ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(bottom = 16.dp)
            ) {
                // Ảnh bìa & Avatar
                Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.height(240.dp)) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Gray)
                    )
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .background(Color.LightGray)
                    )
                }

                // Thông tin Text
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("u/otis_dev_2025", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        // Nút Edit
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.background(Color(0xFFF0F0F0), CircleShape)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // [MỚI] Hiển thị Bio
                    Text(text = bio, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tuổi tài khoản
                    Text("Thành viên từ 2024", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // [MỚI] Thống kê Karma & Follower
                    Row(modifier = Modifier.fillMaxWidth()) {
                        KarmaCard(icon = Icons.Filled.Star, label = "Điểm uy tín", value = "12.5k", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        KarmaCard(icon = Icons.Filled.Person, label = "Followers", value = "340", modifier = Modifier.weight(1f))
                    }
                }
            }

            // Tiêu đề danh sách bài viết
            Text(
                text = "Bài viết",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // --- DANH SÁCH BÀI VIẾT (CHI TIẾT) ---
        // Sử dụng mockPosts và AppPostItem để hiển thị chi tiết thay vì Grid ảnh
        items(mockPosts) { post ->
            AppPostItem(post = post, onItemClick = { /* Xử lý click vào bài viết */ })
        }

        // Padding bottom
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// --- Dialog chỉnh sửa (Cập nhật thêm Bio) ---
@Composable
fun EditProfileDialog(
    context: Context,
    currentName: String,
    currentBio: String, // [MỚI]
    currentAvatar: Any?,
    currentCover: Any?,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?, Uri?) -> Unit // [MỚI] Thêm String cho Bio
) {
    var newName by remember { mutableStateOf(currentName) }
    var newBio by remember { mutableStateOf(currentBio) } // [MỚI]
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var isAvatar by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { if (isAvatar) avatarUri = it else bannerUri = it } }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                // Nhập tên
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // [MỚI] Nhập Bio
                OutlinedTextField(
                    value = newBio,
                    onValueChange = { newBio = it },
                    label = { Text("Tiểu sử (Bio)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(12.dp))

                // Chọn ảnh Avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = avatarUri ?: currentAvatar,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { isAvatar = true; launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text("Đổi Avatar")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Chọn ảnh Bìa
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = bannerUri ?: currentCover,
                        contentDescription = null,
                        modifier = Modifier.width(80.dp).height(45.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { isAvatar = false; launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text("Đổi Bìa")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Actions
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) }
                    Button(onClick = { onSave(newName, newBio, avatarUri, bannerUri) }) { Text("Lưu thay đổi") }
                }
            }
        }
    }
}

// --- Component hiển thị Karma ---
@Composable
fun KarmaCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0079D3))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}