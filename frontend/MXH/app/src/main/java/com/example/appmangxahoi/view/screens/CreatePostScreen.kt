package com.example.appmangxahoi.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Data mẫu cho cộng đồng
data class CommunityOption(val id: String, val name: String, val iconUrl: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onContentChange: (Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo by remember { mutableStateOf(false) }

    // --- LOGIC CHỌN CỘNG ĐỒNG ---
    // 1. Trạng thái hiển thị BottomSheet
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 2. Cộng đồng đang chọn (Null = Trang cá nhân)
    var selectedCommunity by remember { mutableStateOf<CommunityOption?>(null) }

    // 3. Mock Data các cộng đồng
    val communities = listOf(
        CommunityOption("1", "r/android_dev", "https://picsum.photos/50"),
        CommunityOption("2", "r/vietnam_travel", "https://picsum.photos/51"),
        CommunityOption("3", "r/meme_daily", "https://picsum.photos/52")
    )

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    LaunchedEffect(title, selectedUri) {
        onContentChange(title.isNotEmpty() || selectedUri != null)
    }

    // --- GIAO DIỆN CHÍNH ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // === 1. NÚT CHỌN NƠI ĐĂNG (MỚI THÊM) ===
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Nút bấm mở BottomSheet
            Surface(
                onClick = { showBottomSheet = true },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0F0F0), // Màu xám nhẹ
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    // Icon (Nếu chọn Profile thì hiện hình người, Cộng đồng thì hiện hình Trái đất)
                    val icon = if (selectedCommunity == null) Icons.Default.Person else Icons.Default.Public
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tên hiển thị
                    Text(
                        text = selectedCommunity?.name ?: "Trang cá nhân (u/otis)", // Nếu null thì hiện Profile
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        // --- PHẦN NHẬP TEXT ---
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Tiêu đề thú vị", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray) },
            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text("Bạn đang nghĩ gì?", fontSize = 16.sp, color = Color.Gray) },
            textStyle = TextStyle(fontSize = 16.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // --- PREVIEW ẢNH/VIDEO (CŨ) ---
        if (selectedUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            ) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )

                if (isVideo) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    )
                }

                IconButton(
                    onClick = { selectedUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(24.dp)
                ) {
                    Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // --- TOOLBAR (CŨ) ---
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AttachmentIcon(Icons.Outlined.Link, onClick = {})
            AttachmentIcon(Icons.Outlined.Image, onClick = {
                isVideo = false
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })
            AttachmentIcon(Icons.Outlined.VideoLibrary, onClick = {
                isVideo = true
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            })
            AttachmentIcon(Icons.Outlined.Poll, onClick = {})
        }
    }

    // === 2. BOTTOM SHEET ĐỂ CHỌN CỘNG ĐỒNG (MỚI THÊM) ===
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Chọn nơi đăng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                // Lựa chọn 1: Trang cá nhân
                ListItem(
                    headlineContent = { Text("Trang cá nhân của bạn", fontWeight = FontWeight.Bold) },
                    leadingContent = {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                    },
                    trailingContent = {
                        // Hiện dấu tick nếu đang chọn
                        if (selectedCommunity == null) {
                            Text("✓", color = Color.Blue, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.clickable {
                        selectedCommunity = null // Chọn trang cá nhân
                        showBottomSheet = false
                    }
                )

                HorizontalDivider()

                Text("Cộng đồng của bạn", modifier = Modifier.padding(16.dp), color = Color.Gray)

                // Lựa chọn 2: Danh sách cộng đồng
                LazyColumn {
                    items(communities) { community ->
                        ListItem(
                            headlineContent = { Text(community.name) },
                            leadingContent = {
                                AsyncImage(
                                    model = community.iconUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                                )
                            },
                            trailingContent = {
                                if (selectedCommunity?.id == community.id) {
                                    Text("✓", color = Color.Blue, fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedCommunity = community // Lưu cộng đồng đã chọn
                                showBottomSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentIcon(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
    }
}