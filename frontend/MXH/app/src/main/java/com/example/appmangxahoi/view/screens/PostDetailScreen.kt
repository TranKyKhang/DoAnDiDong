package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.ThumbUp // Icon Like đặc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.PostDetail
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.model.DCPost
import com.example.appmangxahoi.model.UserModel
import com.example.appmangxahoi.view.component.VoteActionPill

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// --- 1. MODEL DỮ LIỆU CÓ CẤU TRÚC PHÂN CẤP ---
data class Comment(
    val id: Int,
    val author: String,
    val avatarUrl: String,
    val content: String,
    val timeAgo: String,
    val initialVoteCount: Int,
    val replies: List<Comment> = emptyList() // List chứa các bình luận con
)

// --- 2. MOCK DATA PHÂN CẤP ---
val mockNestedComments = listOf(
    Comment(
        id = 1, author = "u/thanh_vien_vip", avatarUrl = "https://ui-avatars.com/api/?name=Vip&background=random",
        content = "Bài viết rất hay, cảm ơn bác đã chia sẻ kiến thức bổ ích!", timeAgo = "1h", initialVoteCount = 120,
        replies = listOf(
            Comment(id = 11, author = "u/tac_gia", avatarUrl = "https://ui-avatars.com/api/?name=Author&background=random",
                content = "Cảm ơn bạn nhé, sắp tới mình sẽ ra thêm phần 2.", timeAgo = "45m", initialVoteCount = 50,
                replies = listOf(
                    Comment(id = 111, author = "u/fan_cung", avatarUrl = "https://ui-avatars.com/api/?name=Fan&background=random",
                        content = "Hóng quá bác ơi!", timeAgo = "10m", initialVoteCount = 5)
                )
            ),
            Comment(id = 12, author = "u/nguoi_qua_duong", avatarUrl = "https://ui-avatars.com/api/?name=User&background=random",
                content = "Đồng quan điểm.", timeAgo = "30m", initialVoteCount = 10)
        )
    ),
    Comment(
        id = 2, author = "u/dev_mobile", avatarUrl = "https://ui-avatars.com/api/?name=Dev&background=random",
        content = "Cái này làm bằng Jetpack Compose à? Mượt thế.", timeAgo = "2h", initialVoteCount = 85,
        replies = emptyList()
    ),
    Comment(
        id = 3, author = "u/newbie_coder", avatarUrl = "https://ui-avatars.com/api/?name=New&background=random",
        content = "Cho em xin source code tham khảo với ạ!", timeAgo = "5m", initialVoteCount = 2,
        replies = listOf(
            Comment(id = 31, author = "u/tac_gia", avatarUrl = "https://ui-avatars.com/api/?name=Author&background=random",
                content = "Đã inbox nhé.", timeAgo = "1m", initialVoteCount = 1)
        )
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit
) {
    // 1. STATE QUẢN LÝ DỮ LIỆU TỪ API
    var post by remember { mutableStateOf<DCPost?>(null) }
    var profile by remember { mutableStateOf<UserModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentPost = post
    val currentProfile = profile
    val userController = remember { User() }

    // 2. GỌI API KHI MÀN HÌNH ĐƯỢC MỞ
    LaunchedEffect(postId) {
        // Gọi hàm getPostDetail từ Controller bạn đã viết
        val result = PostDetail().getPostDetail(1)
        post = result
        isLoading = false
    }
    val userId = currentPost?.user_id
    LaunchedEffect(userId) {
        // [SỬA LỖI]: Kiểm tra null trước khi gọi hàm
        if (userId != null) {
            profile = userController.getUserProfile(userId)
        }
    }

    // State để quản lý việc đang reply ai (để hiện tên dưới thanh chat)
    //var replyingTo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(currentPost != null){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar giả lập (vì API hiện tại chưa trả về avatar user)
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=User${currentPost.user_id}&background=random",
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            // Hiển thị tạm ID vì API chưa có tên Author/Community
                            Text("Community #${currentPost.community_id}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val displayName = if (currentProfile != null) {
                                currentProfile.displayName ?: "User ${currentPost.user_id}"
                            } else {
                                "Đang tải..." // Hoặc hiện User ID tạm
                            }
                            Text(
                                text = "$displayName | ${formatIsoDate(currentPost.created_at)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }}
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
//        bottomBar = {
//            CommentInputBar(replyingTo = replyingTo, onCancelReply = { replyingTo = null })
//        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)){
            // --- TRƯỜNG HỢP 1: ĐANG TẢI ---
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // --- TRƯỜNG HỢP 2: LỖI HOẶC KHÔNG CÓ DỮ LIỆU ---
            else if (currentPost == null) {
                Text("Lỗi tải bài viết hoặc bài viết không tồn tại!", modifier = Modifier.align(Alignment.Center))
            }else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Color.White)
                ){
                    item {
                        Column(modifier = Modifier.padding(16.dp)){
                            Text(
                                text = currentPost.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!currentPost.content.isNullOrEmpty()) {
                                Text(text = currentPost.content, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                            }

//                            val firstImage = currentPost.images.firstOrNull()?.image
//                            if (currentPost.video.isNullOrEmpty() && firstImage != null) {
//                                AsyncImage(
//                                    model = firstImage,
//                                    contentDescription = null,
//                                    contentScale = ContentScale.FillWidth,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .clip(RoundedCornerShape(8.dp))
//                                )
//                                Spacer(modifier = Modifier.height(12.dp))
//                            }
                            if (currentPost.video.isNullOrEmpty() && currentPost.images.isNotEmpty()) {
                                currentPost.images.forEach { postImage ->
                                    AsyncImage(
                                        model = postImage.image, // URL ảnh
                                        contentDescription = null,
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp) // Khoảng cách giữa các ảnh
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Vote count (API trả về Int)
                                VoteActionPill(
                                    upvotes = currentPost.upvotes.toString(),
                                    downvotes = currentPost.downvotes.toString()
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Comment count
                                    Text(currentPost.comment_count.toString(), color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        HorizontalDivider(thickness = 8.dp, color = Color(0xFFF2F2F2))
                    }
                }
            }
        }
    }
}

// --- 3. COMPONENT ĐỆ QUY VẼ CÂY COMMENT ---
@Composable
fun CommentTree(
    comment: Comment,
    depth: Int = 0, // Độ sâu của comment (để tính thụt lề)
    onReplyClick: (String) -> Unit
) {
    Column {
        // Vẽ bản thân comment hiện tại
        CommentItem(comment, depth, onReplyClick)

        // Nếu có comment con (replies), tiếp tục vẽ đệ quy
        if (comment.replies.isNotEmpty()) {
            comment.replies.forEach { reply ->
                CommentTree(
                    comment = reply,
                    depth = depth + 1, // Tăng độ sâu lên 1
                    onReplyClick = onReplyClick
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    depth: Int,
    onReplyClick: (String) -> Unit
) {
    // State quản lý Like cục bộ
    var isLiked by remember { mutableStateOf(false) }
    var currentVoteCount by remember { mutableIntStateOf(comment.initialVoteCount) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (depth % 2 == 1) Color(0xFFFAFAFA) else Color.White) // Đổi màu nền nhẹ nếu là cấp con
            .padding(
                // Logic thụt lề: Cấp 0 (16dp), Cấp 1 (16+32dp), v.v.
                start = (16 + (depth * 32)).dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 12.dp
            )
    ) {
        // Avatar
        AsyncImage(
            model = comment.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Nội dung
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("• ${comment.timeAgo}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.content, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // --- ACTION BUTTONS (LIKE, REPLY) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Nút Reply
                Row(
                    modifier = Modifier.clickable { onReplyClick(comment.author) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reply", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Nút Like (Có logic đổi màu và số lượng)
                Row(
//                    modifier = Modifier.clickable {
//                        isLiked = !isLiked
//                        if (isLiked) currentVoteCount++ else currentVoteCount--
//                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = null,
                        //modifier = Modifier.size(16.dp),
                        tint = if (isLiked) Color(0xFF0079D3) else Color.Gray // Xanh nếu like, Xám nếu chưa
                    )
                    //Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentVoteCount", // Hiển thị số vote động
                        fontSize = 12.sp,
                        color = if (isLiked) Color(0xFF0079D3) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                // Nút Dislike (Icon trang trí)
                Icon(Icons.Outlined.ThumbDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}

// Thanh nhập comment được nâng cấp để hiển thị trạng thái Reply
@Composable
fun CommentInputBar(
    replyingTo: String?,
    onCancelReply: () -> Unit
) {
    Column {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        // Hiển thị dòng "Đang trả lời..." nếu có
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Đang trả lời $replyingTo", fontSize = 12.sp, color = Color.Gray)
                Text(
                    "Hủy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.clickable { onCancelReply() }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(if (replyingTo != null) "Trả lời $replyingTo..." else "Viết bình luận...") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF2F2F2)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(onClick = {}) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF0079D3))
            }
        }
    }
}
fun formatIsoDate(isoDate: String): String {
    return try {
        // Dùng java.util.Locale.getDefault() cho đúng chuẩn
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate)

        val outputFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        outputFormat.format(date ?: return isoDate)
    } catch (e: Exception) {
        isoDate
    }
}