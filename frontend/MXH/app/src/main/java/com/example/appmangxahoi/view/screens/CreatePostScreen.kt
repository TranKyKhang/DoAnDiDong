package com.example.appmangxahoi.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
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

@Composable
fun CreatePostScreen(
    onContentChange: (Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    // 1. Biến lưu đường dẫn ảnh/video được chọn (Uri)
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    // 2. Biến đánh dấu xem đang chọn Video hay Ảnh
    var isVideo by remember { mutableStateOf(false) }

    // 3. Công cụ mở thư viện (Photo Picker)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // Khi người dùng chọn xong, lưu Uri lại
        if (uri != null) {
            selectedUri = uri
        }
    }

    // Logic kiểm tra nút "Đăng" (Có tiêu đề HOẶC có ảnh thì mới cho đăng)
    LaunchedEffect(title, selectedUri) {
        onContentChange(title.isNotEmpty() || selectedUri != null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
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

        // --- 4. KHU VỰC PREVIEW (HIỂN THỊ ẢNH/VIDEO ĐÃ CHỌN) ---
        if (selectedUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Chiều cao khung xem trước
                    .padding(vertical = 8.dp)
            ) {
                // Hiển thị ảnh (Hoặc ảnh bìa video)
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )

                // Nếu là Video -> Hiện thêm nút Play ở giữa để nhận biết
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

                // Nút Xóa ảnh (Góc trên phải)
                IconButton(
                    onClick = { selectedUri = null }, // Xóa Uri đi
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

        // --- 5. THANH CÔNG CỤ ---
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AttachmentIcon(Icons.Outlined.Link, onClick = {})

            // Nút chọn ẢNH
            AttachmentIcon(Icons.Outlined.Image, onClick = {
                isVideo = false // Đánh dấu là ảnh
                // Mở thư viện chỉ lọc ảnh
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })

            // Nút chọn VIDEO
            AttachmentIcon(Icons.Outlined.VideoLibrary, onClick = {
                isVideo = true // Đánh dấu là video
                // Mở thư viện chỉ lọc video
                mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            })

            AttachmentIcon(Icons.Outlined.Poll, onClick = {})
        }
    }
}

// Cập nhật hàm AttachmentIcon để nhận sự kiện click
@Composable
fun AttachmentIcon(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
    }
}