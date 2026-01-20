package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF2F4F8), // Màu nền xám nhẹ
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Mục: Đổi mật khẩu
            SettingItem(
                icon = Icons.Default.Lock,
                title = "Đổi mật khẩu",
                onClick = onChangePasswordClick
            )

            Spacer(modifier = Modifier.height(2.dp)) // Khoảng cách nhỏ giữa các item

            // Mục: Đăng xuất
            SettingItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Đăng xuất",
                titleColor = Color.Red,
                iconColor = Color.Red,
                showArrow = false,
                onClick = onLogoutClick
            )
        }
    }
}

// Component con để tái sử dụng, giúp code chính gọn gàng hơn
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Black,
    iconColor: Color = Color.Gray,
    showArrow: Boolean = true
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )

            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}