package com.example.appmangxahoi.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
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

@Composable
fun ProfileScreen() {
    // 1. CÁC BIẾN TRẠNG THÁI (STATE) ĐỂ LƯU THÔNG TIN
    var name by remember { mutableStateOf("Otis Dev") }
    var avatarUri by remember { mutableStateOf<Any>("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200") }
    var coverUri by remember { mutableStateOf<Any>("https://picsum.photos/800/400") }
    var showEditDialog by remember { mutableStateOf(false) }

    // 2. HIỂN THỊ DIALOG CHỈNH SỬA NẾU ĐƯỢC KÍCH HOẠT
    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            currentAvatar = avatarUri,
            currentCover = coverUri,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatar, newCover ->
                name = newName
                if (newAvatar != null) avatarUri = newAvatar
                if (newCover != null) coverUri = newCover
                showEditDialog = false
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // --- ITEM 1: HEADER ---
        item(span = { GridItemSpan(3) }) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // === KHỐI BOX TẠO HIỆU ỨNG CHỒNG ẢNH ===
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier.height(240.dp)
                ) {
                    // 1. Ảnh bìa (Dùng biến coverUri)
                    AsyncImage(
                        model = coverUri,
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.TopCenter)
                            .background(Color.Gray)
                    )

                    // 2. Avatar (Dùng biến avatarUri)
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

                // Container chứa phần text thông tin bên dưới
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // --- 3. SỬA ĐỔI PHẦN HIỂN THỊ TÊN ĐỂ THÊM NÚT EDIT ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Hiển thị tên (Dùng biến name)
                            Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "u/otis_dev_2025", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }

                        // Nút Edit
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.background(Color(0xFFF0F0F0), CircleShape)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.Black)
                        }
                    }

                    Text(text = "Thành viên từ 2024", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 3.dp, color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phần thống kê (Karma)
                    Text("Thống kê", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KarmaCard(icon = Icons.Filled.Star, label = "Điểm uy tín", value = "12.5k", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        KarmaCard(icon = Icons.Filled.Person, label = "Followers", value = "340", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Bài viết", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }

        // --- ITEM 2: GRID BÀI VIẾT ---
        items(21) { index ->
            AsyncImage(
                model = "https://picsum.photos/300?random=$index",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(Color(0xFFEEEEEE))
            )
        }
    }
}

// --- 4. COMPONENT DIALOG CHỈNH SỬA ---
@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: Any,
    currentCover: Any,
    onDismiss: () -> Unit,
    onSave: (String, Any?, Any?) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    // Biến tạm để lưu ảnh mới chọn (nếu có)
    var tempAvatarUri by remember { mutableStateOf<Any?>(null) }
    var tempCoverUri by remember { mutableStateOf<Any?>(null) }

    // Biến xác định đang chọn Avatar hay Cover
    var isPickingAvatar by remember { mutableStateOf(true) }

    // Launcher chọn ảnh
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            if (isPickingAvatar) {
                tempAvatarUri = uri
            } else {
                tempCoverUri = uri
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Chỉnh sửa hồ sơ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

                // --- Input Tên ---
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tên hiển thị") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Chọn Avatar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hiển thị preview avatar mới (hoặc cũ)
                        AsyncImage(
                            model = tempAvatarUri ?: currentAvatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Ảnh đại diện", fontWeight = FontWeight.Medium)
                    }
                    TextButton(onClick = {
                        isPickingAvatar = true
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đổi")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

                // --- Chọn Ảnh bìa ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hiển thị preview cover mới (hoặc cũ)
                        AsyncImage(
                            model = tempCoverUri ?: currentCover,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(width = 80.dp, height = 40.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Ảnh bìa", fontWeight = FontWeight.Medium)
                    }
                    TextButton(onClick = {
                        isPickingAvatar = false
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đổi")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Nút Hủy / Lưu ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(newName, tempAvatarUri, tempCoverUri) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079D3))
                    ) {
                        Text("Lưu thay đổi")
                    }
                }
            }
        }
    }
}

// Component con: Thẻ hiển thị điểm uy tín (Giữ nguyên)
@Composable
fun KarmaCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = modifier.padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}