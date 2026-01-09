package com.example.appmangxahoi.view.component
// --- CÁC IMPORT CẦN THIẾT CHO JETPACK COMPOSE & MATERIAL 3 ---

// 1. Các thành phần giao diện cơ bản (Layout, Modifier, Graphics)
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// 2. Thư viện Material Design 3 (Card, Text, Button, Icon...)
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// 3. Thư viện Icon (Biểu tượng)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.VerticalDivider

// 4. Thư viện Coil (Để load ảnh từ mạng)
import coil.compose.AsyncImage

// 5. Nếu bạn dùng Preview (tùy chọn)
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import com.example.appmangxahoi.model.DataClassPost

    @Composable
    fun AppPostItem(post: DataClassPost) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White), // Hoặc MaterialTheme.colorScheme.surface
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp) // Khoảng cách giữa các bài
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // --- HEADER: Subreddit + Author + Time ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar subreddit giả
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop, // Cắt ảnh cho vừa khung tròn
                        modifier = Modifier
                            .size(32.dp)           // Kích thước avatar (Reddit thường dùng 32dp hoặc 40dp)
                            .clip(CircleShape)     // Bo tròn thành hình tròn
                            .background(Color.LightGray) // Màu nền hiện trong lúc đang tải ảnh
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

                    // [MỚI] Biến trạng thái: Đang phát hay không?
                    var isPlaying by remember { mutableStateOf(false) }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black) // Nền đen cho video
                    ) {
                        // TRƯỜNG HỢP 1: ĐANG PHÁT VIDEO -> HIỆN PLAYER
                        if (isPlaying && post.videoUrl != null) {
                            VideoPlayer(
                                videoUrl = post.videoUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // TRƯỜNG HỢP 2: CHƯA PHÁT -> HIỆN ẢNH BÌA + NÚT PLAY
                        else {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().alpha(if (post.isVideo) 0.8f else 1f)
                            )

                            // Nếu là bài Video thì hiện nút Play to đùng
                            if (post.isVideo) {
                                IconButton(
                                    onClick = {
                                        isPlaying = true // <--- BẤM VÀO LÀ CHUYỂN TRẠNG THÁI
                                    },
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

                // --- FOOTER: Actions (Vote, Comment, Share) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // Căn lề các nút Comment/Share sang bên phải
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //CỤM NÚT VOTE
                    VoteActionPill(voteCount = post.voteCount)

                    // Comment, Share
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { // Cách nhau 8dp

                        // Viên Comment
                        OutlinedButtonChip(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            text = post.commentCount
                        )

                        // Viên Share
                        OutlinedButtonChip(
                            icon = Icons.Outlined.Share,
                            text = "Share"
                        )
                    }
                }
            }
        }
    }
//Composable like,dislike
@Composable
fun VoteActionPill(voteCount: String) {
    val pillShape = RoundedCornerShape(50) // Hình dạng viên thuốc (bo tròn tối đa)
    val iconSize = 20.dp // Kích thước icon nhỏ gọn

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            // Tạo viền màu xám nhạt
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
            .padding(horizontal = 4.dp, vertical = 2.dp) // Padding bên trong viên thuốc để nó không bị sát quá
    ) {
        // Nút Upvote
        IconButton(onClick = { /* Xử lý Upvote */ }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.ThumbUp,
                contentDescription = "Upvote",
                modifier = Modifier.size(iconSize),
                tint = Color.Gray // Màu xám khi chưa bấm
            )
        }

        // Số Vote
        Text(
            text = voteCount,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 2.dp) // Khoảng cách rất nhỏ giữa số và icon
        )
        //Dau gach o giua
        VerticalDivider(
            thickness = 1.dp,       // Độ dày của nét
            color = Color.LightGray,// Màu xám nhạt
            modifier = Modifier
                .height(14.dp)      // Chiều cao của gạch (ngắn hơn chiều cao nút)
                .padding(horizontal = 2.dp) // Khoảng cách 2 bên gạch
        )
        // Nút Downvote
        IconButton(onClick = { /* Xử lý Downvote */ }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.ThumbDown,
                contentDescription = "Downvote",
                modifier = Modifier.size(iconSize),
                tint = Color.Gray
            )
        }
    }
}

//Composable comment,share
@Composable
fun OutlinedButtonChip(icon: ImageVector, text: String) {
    val pillShape = RoundedCornerShape(50) // Bo tròn

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, pillShape) // Viền xám
            .padding(horizontal = 12.dp, vertical = 6.dp) // Padding bên trong để nút trông đầy đặn
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp) // Icon nhỏ xinh
        )

        Spacer(modifier = Modifier.width(6.dp)) // Khoảng cách giữa Icon và Chữ

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}

// Hàm phụ để vẽ nút bấm cho gọn code
@Composable
fun ActionItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}