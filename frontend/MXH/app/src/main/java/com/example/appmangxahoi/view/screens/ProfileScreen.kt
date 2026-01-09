// --- ProfileScreen.kt ---
package com.example.appmangxahoi.view.screens

import android.content.Context
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
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.model.UserModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(context: Context, userId: Int) {
    val userController = remember { User() }
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf<UserModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Load profile
    LaunchedEffect(userId) {
        profile = userController.getUserProfile(userId)
    }

    // State hiển thị UI
    var name by remember { mutableStateOf("Otis Dev") }
    var avatarUri by remember { mutableStateOf<Any>("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200") }
    var coverUri by remember { mutableStateOf<Any>("https://picsum.photos/800/400") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.displayName ?: "No name"
            avatarUri = it.avatar?.let { path -> "http://10.0.2.2:3000$path" } ?: ""
            coverUri = it.banner?.let { path -> "http://10.0.2.2:3000$path" } ?: ""
        }
    }

    // Dialog chỉnh sửa
    if (showEditDialog) {
        EditProfileDialog(
            context = context,
            currentName = name,
            currentAvatar = avatarUri,
            currentCover = coverUri,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatar, newCover ->
                // gọi API
                scope.launch {
                    val success = userController.updateProfile(
                        context,
                        userId,
                        newName,
                        null,
                        newAvatar,
                        newCover
                    )
                    if (success) {
                        // update UI nếu thành công
                        name = newName
                        if (newAvatar != null) avatarUri = newAvatar
                        if (newCover != null) coverUri = newCover
                    }
                    showEditDialog = false
                }
            }
        )
    }

    // UI chính
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.height(240.dp)) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Gray)
                    )
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(start = 16.dp).size(80.dp)
                            .clip(CircleShape).border(3.dp, Color.White, CircleShape).background(Color.LightGray)
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("u/otis_dev_2025", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        IconButton(onClick = { showEditDialog = true }, modifier = Modifier.background(Color(0xFFF0F0F0), CircleShape)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Black)
                        }
                    }
                    Text("Thành viên từ 2024", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        items(21) { index ->
            AsyncImage(
                model = "https://picsum.photos/300?random=$index",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.aspectRatio(1f).background(Color(0xFFEEEEEE))
            )
        }
    }
}

// --- Dialog chỉnh sửa ---
@Composable
fun EditProfileDialog(
    context: Context,
    currentName: String,
    currentAvatar: Any?,
    currentCover: Any?,
    onDismiss: () -> Unit,
    onSave: (String, Uri?, Uri?) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var isAvatar by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { if (isAvatar) avatarUri = it else bannerUri = it } }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                AsyncImage(model = avatarUri ?: currentAvatar, contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(CircleShape))
                TextButton(onClick = { isAvatar = true; launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Đổi avatar")
                }
                Spacer(Modifier.height(8.dp))
                AsyncImage(model = bannerUri ?: currentCover, contentDescription = null, modifier = Modifier.fillMaxWidth().height(60.dp))
                TextButton(onClick = { isAvatar = false; launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Đổi banner")
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Button(onClick = { onSave(newName, avatarUri, bannerUri) }) { Text("Lưu") }
                }
            }
        }
    }
}

// --- Component nhỏ hiển thị Karma ---
@Composable
fun KarmaCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), modifier = modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
