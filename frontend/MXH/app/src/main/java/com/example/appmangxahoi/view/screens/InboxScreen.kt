package com.example.appmangxahoi.view.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmangxahoi.controller.Notification
import com.example.appmangxahoi.model.DataClassNotification
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material3.ripple

@Composable
fun InboxScreen(
    token: String,
    topPadding: Dp = 0.dp,
    onNotificationClick: (DataClassNotification) -> Unit
) {
    val notificationController = remember { Notification() }
    var notifications by remember { mutableStateOf<List<DataClassNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        var result = notificationController.getNotifications(token)
        if (result != null){
            notifications = result
        }
        isLoading = false
    }
    val displayNotifications = notifications
        .sortedByDescending { it.created_at }
        .distinctBy {
            Triple(
                it.sender_id,
                it.type,
                it.post_id ?: it.comment_id
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = topPadding)
    ) {
        Text(
            text = "Thông báo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có thông báo nào", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(displayNotifications) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = {
                            if (notification.is_read == 0) {
                                notifications = notifications.map {
                                    if (it.id == notification.id)
                                        it.copy(is_read = 1)
                                    else it
                                }

                                scope.launch {
                                    notificationController.updateRead(token, notification.id)
                                }
                            }
                            onNotificationClick(notification)
                        }
                    )
                    Divider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}
@Composable
fun NotificationRow(
    notification: DataClassNotification,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (notification.is_read == 1) Color.White else Color(0xFFE3F2FD)
    val iconInfo = when (notification.type) {
        "comment" -> Pair(Icons.Filled.Comment, Color(0xFF0079D3))
        "vote" -> Pair(Icons.Filled.ThumbUp, Color(0xFFFF4500))
        "post" -> Pair(Icons.Filled.Article, Color(0xFF2E7D32))
        else -> Pair(Icons.Filled.Notifications, Color.Gray)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF0079D3)),
            contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconInfo.first,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(notification.content)
                }
            }, fontSize = 16.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimeAgo(notification.created_at.toString()),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
fun formatTimeAgo(isoString: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.parse(isoString)
            val now = Instant.now()

            val diffSeconds = ChronoUnit.SECONDS.between(instant, now)
            val diffMinutes = ChronoUnit.MINUTES.between(instant, now)
            val diffHours = ChronoUnit.HOURS.between(instant, now)
            val diffDays = ChronoUnit.DAYS.between(instant, now)

            when {
                diffSeconds < 60 -> "Vừa xong"
                diffMinutes < 60 -> "$diffMinutes phút trước"
                diffHours < 24 -> "$diffHours giờ trước"
                diffDays < 7 -> "$diffDays ngày trước"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.systemDefault())
                    formatter.format(instant)
                }
            }
        } else {
            isoString.take(10)
        }
    } catch (e: Exception) {
        isoString
    }
}