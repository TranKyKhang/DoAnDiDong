package com.example.appmangxahoi.view.screens

import android.os.Build
import android.widget.Toast
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
import androidx.compose.ui.text.LinkAnnotation
import com.example.appmangxahoi.controller.Communities
import com.example.appmangxahoi.controller.Notification
import com.example.appmangxahoi.model.DataClassCommunities
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    var community by remember { mutableStateOf<DataClassCommunities?>(null) }
    var profile by remember { mutableStateOf<UserModel?>(null) }
    var commentsList by remember { mutableStateOf<List<DataClassComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var replyToComment by remember { mutableStateOf<DataClassComment?>(null) }

    val currentPost = post
    val currentCommunity = community
    val currentProfile = profile

    val commentController = remember { Comment() }
    val userController = remember { User() }
    val communityController = remember { Communities() }
    val notificationController = remember { Notification() }

    val scope = rememberCoroutineScope()
    val currentUserId = 6
    ///
    fun refreshComments() {
        scope.launch {
            val newComments = commentController.getComments(postId)
            if (newComments != null) commentsList = newComments
        }
    }
    fun refreshData() {
        scope.launch {
            // A. Tải lại danh sách comment mới
            val newComments = commentController.getComments(postId)
            if (newComments != null) {
                commentsList = newComments
            }

            // B. Tải lại chi tiết bài viết (để cập nhật số lượng comment_count)
            val updatedPost = PostDetail().getPostDetail(postId)
            if (updatedPost != null) {
                post = updatedPost
            }
        }
    }
    // 2. GỌI API KHI MÀN HÌNH ĐƯỢC MỞ
    LaunchedEffect(postId) {
        // Gọi hàm getPostDetail từ Controller bạn đã viết
        val result = PostDetail().getPostDetail(postId)
        post = result
        if (result != null) {
            val commentsResult = commentController.getComments(result.id)
            if (commentsResult != null) {
                commentsList = commentsResult
            }

            // 3. Lấy thông tin user của bài viết
            val userResult = userController.getUserProfile(result.user_id)
            profile = userResult
            val communityResult = communityController.getDetail(result.community_id)
            community = communityResult
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
                            Text("${currentCommunity?.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
        bottomBar = {
            CommentInputBar(
                reply = replyToComment,
                onCancel = { replyToComment = null },
                onSendClick = {content ->
                    scope.launch {
                        val parentId = replyToComment?.id
                        val success = commentController.postComment(postId, currentUserId, content, parentId)

                        if (success) {
                            val recipientId = if (replyToComment != null) {
                                replyToComment!!.user_id // Nếu reply -> Gửi cho chủ comment
                            } else {
                                currentPost?.user_id ?: 0 // Nếu comment gốc -> Gửi cho chủ bài viết
                            }
                            if (recipientId != 0 && recipientId != currentUserId){
                                val notiContent = if (replyToComment != null) {
                                    "$userId đã trả lời bình luận của bạn: \"$content\""
                                } else {
                                    "$userId đã bình luận bài viết của bạn: \"$content\""
                                }
                                notificationController.postNotification(
                                    type = "comment",
                                    content = notiContent,
                                    recipientId = recipientId,
                                    senderId = currentUserId,
                                    postId = postId,
                                    commentId = parentId // Lưu lại ID comment cha nếu có
                                )
                            }
                            refreshData()
                            replyToComment = null
                            //refreshComments()
                        } else {
                            //Toast.makeText(it, "", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
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
                                    val BASE_URL = "http://10.0.2.2:3000"
                                    val fullImageUrl = "$BASE_URL${postImage.image}"
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
                            CommentItem(
                                comment = comment,
                                onReplyClick = {selected ->
                                    replyToComment = selected
                                }
                            )

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
    comment: DataClassComment,
    onReplyClick: (DataClassComment) -> Unit
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
                            indication = null
                        ) {
                            onReplyClick(comment)
                        }
                    )
                }

            }
        }
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
    }
}
@Composable
fun CommentInputBar(
    reply: DataClassComment?,
    onCancel: () -> Unit,
    onSendClick: (String) -> Unit
){
    var text by remember { mutableStateOf("") }
    Column {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        if (reply != null){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text("Đang trả lời ${reply.username ?: "User"}", fontSize = 12.sp, color = Color.Gray)
                Text(
                    "Hủy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.clickable { onCancel() }
                )
            }
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(if (reply != null) "Trả lời..." else "Viết bình luận...") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendClick(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) Color(0xFF0079D3)
                            else Color.Gray
                )
            }
        }
    }
}
fun formatIsoDate(isoDate: String): String {
    return try {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val instant = Instant.parse(isoDate)
            val now = Instant.now()
            val seconds = ChronoUnit.SECONDS.between(instant, now)
            val minutes = ChronoUnit.MINUTES.between(instant, now)
            val hours = ChronoUnit.HOURS.between(instant, now)
            val days = ChronoUnit.DAYS.between(instant, now)
            when{
                seconds < 60 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                hours < 60 -> "$hours giờ trước"
                days < 7 -> "$days ngày trước"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.systemDefault())
                    formatter.format(instant)
                }
            }
        }else{
            isoDate.take(10)
        }
    } catch (e: Exception) {
        isoDate
    }
}