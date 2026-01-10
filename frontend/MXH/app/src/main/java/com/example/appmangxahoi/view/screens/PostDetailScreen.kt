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
import com.example.appmangxahoi.controller.Comment
import com.example.appmangxahoi.controller.PostDetail
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.model.DCPost
import com.example.appmangxahoi.model.DataClassComment
import com.example.appmangxahoi.model.UserModel
import com.example.appmangxahoi.view.component.VoteActionPill
import androidx.compose.foundation.interaction.MutableInteractionSource

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit
) {
    // 1. STATE QUẢN LÝ DỮ LIỆU TỪ API
    var post by remember { mutableStateOf<DCPost?>(null) }
    var profile by remember { mutableStateOf<UserModel?>(null) }
    var commentsList by remember { mutableStateOf<List<DataClassComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentPost = post
    val currentProfile = profile
    val commentController = remember { Comment() }
    val userController = remember { User() }

    // 2. GỌI API KHI MÀN HÌNH ĐƯỢC MỞ
    LaunchedEffect(postId) {
        // Gọi hàm getPostDetail từ Controller bạn đã viết
        val result = PostDetail().getPostDetail(1)
        post = result
        if (result != null) {
            val commentsResult = commentController.getComments(result.id)
            if (commentsResult != null) {
                commentsList = commentsResult
            }

            // 3. Lấy thông tin user của bài viết
            val userResult = userController.getUserProfile(result.user_id)
            profile = userResult
        }
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
                    if (commentsList.isEmpty()) {
                        item {
                            Text(
                                "Chưa có bình luận nào.",
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }else {
                        items(commentsList) { comment ->
                            CommentItem(comment = comment)

                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }

                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: DataClassComment
) {
    // Tính toán độ thụt lề: Cấp 0 = 16dp, Cấp 1 = 16 + 32 = 48dp...
    val paddingLeft = (16 + (comment.depth_level * 32)).dp

    // Màu nền đổi nhẹ để phân biệt
    val backgroundColor = if (comment.depth_level % 2 == 0) Color.White else Color(0xFFF8F9FA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(
                start = paddingLeft, // <--- THỤT LỀ Ở ĐÂY
                top = 8.dp,
                bottom = 8.dp,
                end = 16.dp
            )
    ) {
        Row(verticalAlignment = Alignment.Top) { // Căn Top để avatar không bị lệch
            // Avatar
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=${comment.username ?: "User"}&background=random&size=128",
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp) // Bé lại chút cho đẹp
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tên user + Thời gian
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.username ?: "User #${comment.user_id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatIsoDate(comment.created_at),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                // Nội dung comment
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vote count (API trả về Int)
                    VoteActionPill(
                        upvotes = comment.upvotes.toString(),
                        downvotes = comment.downvotes.toString()
                    )
                    val interactionSource = remember { MutableInteractionSource() }
                    Text(
                        text = "Trả lời",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null // Tắt hiệu ứng ripple tạm thời để tránh crash
                        ) {
                            /* Handle reply logic */
                        }
                    )
                }

            }
        }
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
    }
}
// Thanh nhập comment được nâng cấp để hiển thị trạng thái Reply
//@Composable
//fun CommentInputBar(
//    replyingTo: String?,
//    onCancelReply: () -> Unit
//) {
//    Column {
//        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
//
//        // Hiển thị dòng "Đang trả lời..." nếu có
//        if (replyingTo != null) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFF2F2F2))
//                    .padding(horizontal = 16.dp, vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Đang trả lời $replyingTo", fontSize = 12.sp, color = Color.Gray)
//                Text(
//                    "Hủy",
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Red,
//                    modifier = Modifier.clickable { onCancelReply() }
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White)
//                .padding(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            TextField(
//                value = "",
//                onValueChange = {},
//                placeholder = { Text(if (replyingTo != null) "Trả lời $replyingTo..." else "Viết bình luận...") },
//                modifier = Modifier
//                    .weight(1f)
//                    .clip(RoundedCornerShape(24.dp))
//                    .background(Color(0xFFF2F2F2)),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color(0xFFF2F2F2),
//                    unfocusedContainerColor = Color(0xFFF2F2F2),
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                shape = RoundedCornerShape(24.dp)
//            )
//            IconButton(onClick = {}) {
//                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF0079D3))
//            }
//        }
//    }
//}
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