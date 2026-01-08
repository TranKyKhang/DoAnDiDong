package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp // Nhớ import Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class NotificationItem(val id: Int, val title: String, val body: String, val time: String, val icon: ImageVector = Icons.Filled.Notifications, val isRead: Boolean = false)
val mockNotifications = listOf(
    NotificationItem(1, "u/Mèo_Béo đã bình luận...", "Bài viết hay quá...", "2 giờ trước"),
    NotificationItem(2, "Chào mừng đến với r/Kotlin", "Cảm ơn bạn đã tham gia...", "1 ngày trước", isRead = true),
    NotificationItem(3, "Đề xuất: r/AndroidDev", "Có thể bạn sẽ thích...", "3 ngày trước", isRead = true)
)

// 3. Màn hình chính
@Composable
fun InboxScreen(
    topPadding: Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = topPadding)
    ) {
        // Tiêu đề nhỏ đầu trang
        Text(
            text = "Hôm nay",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )

        // Danh sách thông báo
        LazyColumn {
            items(mockNotifications) { notification ->
                NotificationRow(notification)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}


@Composable
fun NotificationRow(notification: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.White else Color(0xFFE3F2FD))
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF0079D3)), contentAlignment = Alignment.Center) {
            Icon(imageVector = notification.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(notification.title + "\n") }
                withStyle(style = SpanStyle(color = Color.DarkGray)) { append(notification.body) }
            }, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.time, fontSize = 12.sp, color = Color.Gray)
        }
        IconButton(onClick = {}, modifier = Modifier.size(24.dp).padding(top = 4.dp)) {
            Icon(Icons.Filled.MoreHoriz, contentDescription = "More", tint = Color.Gray)
        }
    }
}