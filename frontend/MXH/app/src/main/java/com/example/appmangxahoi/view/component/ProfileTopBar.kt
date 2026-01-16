package com.example.appmangxahoi.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Icon mũi tên quay lại
import androidx.compose.material.icons.filled.Settings // Icon cài đặt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onBackClick: () -> Unit,      // Hàm xử lý khi bấm nút Back
    onSettingsClick: () -> Unit   // Hàm xử lý khi bấm nút Cài đặt
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
        // 1. Nút Back bên trái
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        // 2. Tiêu đề
        title = {
            Text("Trang cá nhân", fontWeight = FontWeight.Bold)
        },
        // 3. Nút Setting bên phải
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}