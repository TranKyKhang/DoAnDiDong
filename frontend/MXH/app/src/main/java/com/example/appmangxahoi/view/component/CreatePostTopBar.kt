package com.example.appmangxahoi.view.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostTopBar(
    onCloseClick: () -> Unit,
    onPostClick: () -> Unit,
    isPostEnabled: Boolean // Nút Đăng chỉ sáng lên khi đã nhập chữ
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
        title = {
            Text("Tạo bài viết", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        actions = {
            // Nút Đăng
            Button(
                onClick = onPostClick,
                enabled = isPostEnabled, // Chỉ bấm được khi có nội dung
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0079D3), // Màu xanh Reddit
                    disabledContainerColor = Color.LightGray
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Đăng", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}