package com.example.appmangxahoi.view.component

// --- CÁC IMPORT CẦN THIẾT ---
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appmangxahoi.model.DataClassPost

@Composable
fun AppPostItem(post: DataClassPost, onItemClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onItemClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${post.subreddit} • ${post.timeAgo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BODY: Title ---
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // --- IMAGE / VIDEO SECTION ---
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                var isPlaying by remember { mutableStateOf(false) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    if (isPlaying && post.videoUrl != null) {
                        VideoPlayer(
                            videoUrl = post.videoUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(if (post.isVideo) 0.8f else 1f)
                        )

                        if (post.isVideo) {
                            IconButton(
                                onClick = { isPlaying = true },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- FOOTER: Actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SỬA LỖI Ở ĐÂY: Truyền tham số đúng cho hàm mới
                VoteActionPill(
                    upvotes = post.voteCount, // Dùng voteCount từ DataClassPost
                    downvotes = "0"           // Tạm thời để 0 vì Mock Data chưa có downvotes
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButtonChip(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        text = post.commentCount
                    )
                    OutlinedButtonChip(
                        icon = Icons.Outlined.Share,
                        text = "Share"
                    )
                }
            }
        }
    }
}

// --- SỬA LỖI Ở ĐÂY: Thay voteCount bằng upvotes và downvotes ---
@Composable
fun VoteActionPill(upvotes: String, downvotes: String) {
    val pillShape = RoundedCornerShape(50)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // --- UPVOTE ---
        Icon(
            imageVector = Icons.Outlined.ThumbUp,
            contentDescription = "Upvote",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Sửa: Dùng biến upvotes được truyền vào
        Text(
            text = upvotes,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp
        )

        // --- ĐƯỜNG KẺ NGĂN CÁCH ---
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // --- DOWNVOTE ---
        Icon(
            imageVector = Icons.Outlined.ThumbDown,
            contentDescription = "Downvote",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Sửa: Dùng biến downvotes được truyền vào
        Text(
            text = downvotes,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun OutlinedButtonChip(icon: ImageVector, text: String) {
    val pillShape = RoundedCornerShape(50)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}