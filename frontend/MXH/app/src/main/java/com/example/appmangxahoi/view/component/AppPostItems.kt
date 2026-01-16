package com.example.appmangxahoi.view.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.post

import com.example.appmangxahoi.model.PostModel
import kotlinx.coroutines.launch

// Đặt Base URL của bạn ở đây (IP máy tính hoặc Domain server)
const val BASE_URL = "http://10.0.2.2:3000" // Thay IP thật của bạn vào

@Composable
fun AppPostItem(post: PostModel) {
    // Xử lý dữ liệu URL ảnh/video
    val avatarUrl = post.authorAvatarUrl?.let { "$BASE_URL$it" }
    val videoFullUrl = post.video?.let { "$BASE_URL$it" }
    // Lấy ảnh đầu tiên trong mảng images làm ảnh hiển thị hoặc thumbnail video
    val firstImageUrl = post.images.firstOrNull()?.let { "$BASE_URL$it" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- HEADER: Subreddit + Author + Time ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Author
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "r/${post.communityName}", // Thêm prefix r/ cho giống Reddit
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "u/${post.authorName} • ${post.createdAt}", // Format lại ngày tháng sau nhé
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BODY: Title & Content ---
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3, // Giới hạn dòng nếu dài quá
                    color = Color.DarkGray
                )
            }
            // ===== HIỂN THỊ LINK URL =====
            if (!post.linkUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.linkUrl,
                    color = Color(0xFF0079D3), // xanh giống Reddit
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            // --- IMAGE / VIDEO SECTION ---
            // Ưu tiên hiển thị Video trước
            if (videoFullUrl != null) {
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
                    if (isPlaying) {
                        // Gọi hàm Player (sẽ implement sau)
                        VideoPlayer(videoUrl = videoFullUrl)
                    } else {
                        // Hiển thị Thumbnail (là ảnh đầu tiên của post) + Nút Play
//                        if (firstImageUrl != null) {
//                            AsyncImage(
//                                model = firstImageUrl,
//                                contentDescription = null,
//                                contentScale = ContentScale.Crop,
//                                modifier = Modifier.fillMaxSize().alpha(0.7f)
//                            )
//                        }

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
            // Nếu không có Video thì kiểm tra có Ảnh không
            else if (firstImageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))

                AsyncImage(
                    model = firstImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Chiều cao linh hoạt
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- FOOTER: Actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tính toán số vote hiển thị (Up - Down)
                val voteScore = post.rating
                VoteActionPill(voteCount = voteScore, status = post.userVoteStatus, postId = post.id)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Comment (Chuyển Int sang String)
                    OutlinedButtonChip(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        text = post.commentCount.toString()
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

// --- CÁC COMPOSABLE PHỤ TRỢ ---

@Composable
fun VoteActionPill(postId: Int, voteCount: Int, status: String?) {
    Log.d("voteStatus", status.toString())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var postController = remember { post() }
    var id = remember { mutableIntStateOf(postId) }
    var count = remember { mutableStateOf(voteCount) }
    var isUpvoted by remember { mutableStateOf(status?.replace("\"", "") == "upvote") }
    var isDownvoted by remember { mutableStateOf(status?.replace("\"", "") == "downvote") }

    val pillShape = RoundedCornerShape(50)
    val iconSize = 20.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(onClick = {
            scope.launch {
                postController.handleVote(context,id.value,"upvote")
            }

            when {
                isUpvoted -> {
                    count.value--
                    isUpvoted = false
                }

                isDownvoted -> {
                    count.value += 2
                    isDownvoted = false
                    isUpvoted = true
                }

                else -> {
                    count.value++
                    isUpvoted = true
                }
            }
        }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.ThumbUp,
                contentDescription = "Upvote",
                modifier = Modifier.size(iconSize),
                tint = if (isUpvoted) Color.Blue else Color.Gray
            )
        }

        Text(
            text = count.value.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        VerticalDivider(
            thickness = 1.dp,
            color = Color.LightGray,
            modifier = Modifier.height(14.dp).padding(horizontal = 2.dp)
        )

        IconButton(onClick = {
            scope.launch {
                postController.handleVote(context,id.value,"downvote")
            }

            when {
                isDownvoted -> {
                    count.value++
                    isDownvoted = false
                }
                isUpvoted -> {
                    count.value -= 2
                    isUpvoted = false
                    isDownvoted = true
                }
                else -> {
                    count.value--
                    isDownvoted = true
                }
            }
        }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.ThumbDown,
                contentDescription = "Downvote",
                modifier = Modifier.size(iconSize),
                tint = if (isDownvoted) Color.Red else Color.Gray
            )
        }
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

// Composable giả lập Video Player để không bị lỗi compile
@Composable
fun VideoPlayerPlaceholder(url: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Text(
            text = "Đang phát video...\n$url",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}