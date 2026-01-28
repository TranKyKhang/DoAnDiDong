package com.example.appmangxahoi.view.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.wear.compose.material.ripple
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.PostModel
import kotlinx.coroutines.launch


const val BASE_URL = "http://10.0.2.2:3000"

@Composable
fun AppPostItem(
    onClick: () -> Unit,
    post: PostModel,
    onPostUpdated: () -> Unit
) {
    val avatarUrl = post.authorAvatarUrl?.let { "$BASE_URL$it" }
    val videoFullUrl = post.video?.let { "$BASE_URL$it" }
    val firstImageUrl = post.images.firstOrNull()?.let { "$BASE_URL$it" }

    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val postController = remember { post() }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // --- HEADER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "r/${post.communityName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "u/${post.authorName} • ${post.createdAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa bài viết") },
                            onClick = {
                                menuExpanded = false
                                showEditDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa bài viết", color = Color.Red) },
                            onClick = {
                                menuExpanded = false

                                scope.launch {
                                    val success = postController.removePost(
                                        context = context,
                                        postId = post.id
                                    )

                                    if (success) {
                                        onPostUpdated()
                                        Toast.makeText(context, "Đã xóa bài viết thành công", Toast.LENGTH_SHORT).show()
                                    }else {
                                        Toast.makeText(context, " xóa bài viết thất bại ", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                    }
                }
            }

            // --- TITLE & CONTENT ---
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    color = Color.DarkGray
                )
            }

            // --- LINK ---
            if (!post.linkUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.linkUrl,
                    color = Color(0xFF0079D3),
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            // --- IMAGE / VIDEO ---
            if (videoFullUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                var isPlaying by remember { mutableStateOf(false) }
                val videoInteraction = remember { MutableInteractionSource() }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .clickable(
                            interactionSource = videoInteraction,
                            indication = ripple(),
                            onClick = { isPlaying = true }
                        )
                ) {
                    if (isPlaying) {
                        VideoPlayerPlaceholder(videoFullUrl)
                    } else {
                        firstImageUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }
            } else if (firstImageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = firstImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // --- FOOTER ---
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoteActionPill(postId = post.id, voteCount = post.rating, status = post.userVoteStatus)
                OutlinedButtonChip(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = post.commentCount.toString()
                )
            }
        }
    }

    if (showEditDialog) {

        var editContent by remember { mutableStateOf(post.content) }
        var isLoading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isLoading) showEditDialog = false
            },
            title = { Text("Chỉnh sửa bài viết") },
            text = {
                Column {

                    // ===== TITLE (KHÓA) =====
                    OutlinedTextField(
                        value = post.title,
                        onValueChange = {},
                        label = { Text("Tiêu đề") },
                        enabled = false,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Nội dung") },
                        minLines = 4,
                        enabled = !isLoading
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true

                            val success = postController.updatePost(
                                context = context,
                                postId = post.id,
                                content = editContent
                            )

                            isLoading = false

                            if (success) {
                                onPostUpdated()
                                showEditDialog = false
                            } else {
                                // TODO: show Toast nếu muốn
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = { showEditDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

}

@Composable
fun VoteActionPill(postId: Int, voteCount: Int, status: String?) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val postController = remember { post() }
    var count by remember { mutableStateOf(voteCount) }
    var isUpvoted by remember { mutableStateOf(status?.contains("upvote") == true) }
    var isDownvoted by remember { mutableStateOf(status?.contains("downvote") == true) }
    val upInteraction = remember { MutableInteractionSource() }
    val downInteraction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = {
                scope.launch { postController.handleVote(context, postId, "upvote") }
                when {
                    isUpvoted -> { count--; isUpvoted = false }
                    isDownvoted -> { count += 2; isDownvoted = false; isUpvoted = true }
                    else -> { count++; isUpvoted = true }
                }
            },
            interactionSource = upInteraction
        ) {
            Icon(
                imageVector = Icons.Outlined.ThumbUp,
                contentDescription = "Upvote",
                modifier = Modifier.size(20.dp),
                tint = if (isUpvoted) Color.Blue else Color.Gray
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        VerticalDivider(thickness = 1.dp, color = Color.LightGray, modifier = Modifier.height(14.dp))
        IconButton(
            onClick = {
                scope.launch { postController.handleVote(context, postId, "downvote") }
                when {
                    isDownvoted -> { count++; isDownvoted = false }
                    isUpvoted -> { count -= 2; isUpvoted = false; isDownvoted = true }
                    else -> { count--; isDownvoted = true }
                }
            },
            interactionSource = downInteraction
        ) {
            Icon(
                imageVector = Icons.Outlined.ThumbDown,
                contentDescription = "Downvote",
                modifier = Modifier.size(20.dp),
                tint = if (isDownvoted) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
fun OutlinedButtonChip(icon: ImageVector, text: String) {
    val chipInteraction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(
                interactionSource = chipInteraction,
                indication = ripple(),
                onClick = { /* TODO: Xử lý click chip nếu cần */ }
            )
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = Color.Gray)
    }
}

@Composable
fun VideoPlayerPlaceholder(url: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Text(
            text = "Video Player\n$url",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}