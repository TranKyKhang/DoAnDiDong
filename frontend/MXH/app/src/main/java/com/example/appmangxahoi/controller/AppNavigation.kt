package com.example.appmangxahoi.controller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

// Định nghĩa các màn hình trong App
sealed class Screen(
    val route: String,
    val title: String,
    val unselectedIcon: ImageVector, // Icon viền (khi chưa chọn)
    val selectedIcon: ImageVector    // Icon đặc (khi đã chọn)
    ) {
    object Home : Screen("home", "Trang chủ", Icons.Outlined.Home,Icons.Filled.Home)
    object Create : Screen("create", "Tạo bài", Icons.Outlined.AddCircleOutline,Icons.Filled.AddCircle)
    object Inbox : Screen("inbox", "Hộp thư đến", Icons.Outlined.Notifications,Icons.Filled.Notifications)
    object Profile : Screen("profile", "Cá nhân", Icons.Outlined.Notifications,Icons.Filled.Notifications)

    object Search : Screen("search", "Tìm kiếm", Icons.Outlined.Search, Icons.Filled.Search)
}