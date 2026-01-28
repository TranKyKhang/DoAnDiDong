package com.example.appmangxahoi.view.screens

import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.ThumbUp
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
import com.example.appmangxahoi.model.DCPost
import com.example.appmangxahoi.model.DataClassComment
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.setValue
import com.example.appmangxahoi.controller.Notification
import com.example.appmangxahoi.controller.User
import com.example.appmangxahoi.utils.TokenManager
import com.example.appmangxahoi.utils.UserManager
import com.example.appmangxahoi.view.component.VideoPlayer
import com.example.appmangxahoi.view.component.VoteHelper
import kotlinx.coroutines.launch

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class VoteType {
    none,
    upvote,
    downvote
}
private val BASE_URL = "http://10.0.2.2:3000"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    context: Context,
    postId: Int,
    onBackClick: () -> Unit,
    onCommunityClick: (Int) -> Unit,
    onUserClick: (Int) -> Unit
) {
    // 1. STATE QUẢN LÝ DỮ LIỆU TỪ API
    var post by remember { mutableStateOf<DCPost?>(null) }
    var commentsList by remember { mutableStateOf<List<DataClassComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var replyToComment by remember { mutableStateOf<DataClassComment?>(null) }

    val currentPost = post

    val commentController = remember { Comment() }
    val notificationController = remember { Notification() }
    ///
    ///
    val voteController = remember { com.example.appmangxahoi.controller.Vote() }
    var postVoteState by remember { mutableStateOf(VoteType.none) }
    //
    var upvoteCount by remember { mutableIntStateOf(0) }
    var downvoteCount by remember { mutableIntStateOf(0) }

    //
    val scope = rememberCoroutineScope()
    val token = TokenManager.getToken(context)
    val username = UserManager.getUsername(context)

    if (token == null) {
        // tạm thời cho đồ án
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa đăng nhập")
        }
        return
    }
    ///
    fun refreshComments() {
        scope.launch {
            val newComments = commentController.getComments(token = token, postId)
            if (newComments != null) commentsList = newComments
        }
    }
    fun refreshData() {
        scope.launch {
            // A. Tải lại danh sách comment mới
            val newComments = commentController.getComments(token = token, postId)
            if (newComments != null) {
                commentsList = newComments
            }


            val updatedPost = PostDetail().getPostDetail(token = token, postId)
            if (updatedPost != null) {
                post = updatedPost
            }
        }
    }

    LaunchedEffect(postId) {
        val result = PostDetail().getPostDetail(token = token, postId)
        post = result
        Log.d("My", "${result?.user_vote_status}")
        var typeString = result?.user_vote_status
        if (result != null) {
            val commentsResult = commentController.getComments(token, result.id)
            if (commentsResult != null) {
                commentsList = commentsResult
            }
        }
        isLoading = false

        postVoteState = when (typeString) {
            "upvote" -> VoteType.upvote
            "downvote" -> VoteType.downvote
            else -> VoteType.none
        }
        if (result != null) {
            upvoteCount = result.upvotes ?: 0
            downvoteCount = result.downvotes ?: 0
        }
        ////

    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(currentPost != null){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val avatarUrl = when {
                                currentPost?.author_avatar.isNullOrBlank() -> {
                                    "https://ui-avatars.com/api/?name=${currentPost.author_display_name ?: "User"}&background=random"
                                }
                                else -> {
                                    "$BASE_URL${currentPost?.author_avatar}"
                                }
                            }
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("${currentPost.community_name}",
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable{
                                        if (currentPost.community_id != null) {
                                            onCommunityClick(currentPost.community_id)
                                        }
                                    }
                                )
                                val displayName = if (currentPost.author_id != null) {
                                    currentPost.author_display_name ?: "User ${currentPost.author_id}"
                                } else {
                                    "Đang tải..."
                                }
                                Text(
                                    text = "$displayName | ${formatIsoDate(currentPost.created_at.toString())}",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.clickable{
                                        if(currentPost.author_id != null){
                                            onUserClick(currentPost.author_id)
                                        }
                                    }
                                )
                            }
                        }
                    }
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
                        val success = commentController.postComment(
                            token = token,
                            postId = postId,
                            content = content,
                            parentCommentId = parentId
                        )

                        if (success) {
                            val recipientId = if (replyToComment != null) {
                                replyToComment!!.user_id
                            } else {
                                currentPost?.author_id ?: 0
                            }
                            val senderId = UserManager.getUserId(context)

                            if (recipientId != 0 && recipientId != senderId){
                                val notiContent = if (replyToComment != null) {
                                    "${username} đã trả lời bình luận của bạn"
                                } else {
                                    "${username} đã bình luận bài viết của bạn"
                                }
                                notificationController.postNotification(
                                    token = token,
                                    receiverId = recipientId,
                                    type = "comment",
                                    content = notiContent,
                                    postId = postId,
                                    commentId = parentId
                                )
                            }
                            refreshData()
                            replyToComment = null
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)){
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
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

                            val linkVideo = "$BASE_URL/${currentPost.link_url}"
                            Log.d("My", linkVideo)
                            VideoPlayer(linkVideo)
                            if (currentPost.images.isNotEmpty()) {
                                currentPost.images.forEach { postImage ->
                                    val fullImageUrl = "$BASE_URL${postImage.image}"
                                    AsyncImage(
                                        model = fullImageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text("Link: ${currentPost.link_url}")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VoteActionPill(
                                    upvotes = upvoteCount.toString(),
                                    downvotes = downvoteCount.toString(),
                                    curVote = postVoteState,
                                    onVote = {newType ->
                                        val action = VoteHelper.resolveVote(postVoteState, newType)
                                        if (!action.shouldCallApi) return@VoteActionPill
                                        when (newType) {
                                            VoteType.upvote -> {
                                                upvoteCount++
                                                if (postVoteState == VoteType.downvote) downvoteCount--
                                            }
                                            VoteType.downvote -> {
                                                downvoteCount++
                                                if (postVoteState == VoteType.upvote) upvoteCount--
                                            }
                                            VoteType.none -> {
                                                if (postVoteState == VoteType.upvote) upvoteCount--
                                                if (postVoteState == VoteType.downvote) downvoteCount--
                                            }
                                        }
                                        val oldVote = postVoteState
                                        postVoteState = newType
                                        scope.launch {
                                            val success = voteController.postVote(
                                                token = token,
                                                target = "post",
                                                id = postId,
                                                type = action.apiType!!
                                            )
                                            if(success){
                                                if (oldVote == VoteType.none && newType != VoteType.none){
                                                    val receiverId = currentPost?.author_id
                                                    val senderId = UserManager.getUserId(context)
                                                    val username = UserManager.getUsername(context)
                                                    if (receiverId != null && receiverId != senderId) {
                                                        val content = when (newType) {
                                                            VoteType.upvote -> "$username đã upvote bài viết của bạn"
                                                            VoteType.downvote -> "$username đã downvote bài viết của bạn"
                                                            else -> return@launch
                                                        }
                                                        notificationController.postNotification(
                                                            token = token,
                                                            receiverId = receiverId,
                                                            senderId = senderId,
                                                            type = "vote",
                                                            content = content,
                                                            postId = postId,
                                                            commentId = null
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))

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
                                context = context,
                                comment = comment,
                                token = token,
                                username = username,
                                commentController = commentController,
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
    context: Context,
    comment: DataClassComment,
    token: String,
    username: String?,
    commentController: Comment,
    onReplyClick: (DataClassComment) -> Unit
) {
    val voteController = remember { com.example.appmangxahoi.controller.Vote() }
    var curUpvote by remember { mutableIntStateOf(comment.upvotes) }
    var curDownvote by remember { mutableIntStateOf(comment.downvotes) }
    val notificationController = remember { Notification() }

    val scope = rememberCoroutineScope()
    var commentVoteState by remember {
        mutableStateOf(
            when (comment.user_vote_status) {
                "upvote" -> VoteType.upvote
                "downvote" -> VoteType.downvote
                else -> VoteType.none
            }
        )
    }
    // Tính toán độ thụt lề: Cấp 0 = 16dp, Cấp 1 = 16 + 32 = 48dp...
    val paddingLeft = (16 + (comment.depth_level * 32)).dp

    // Màu nền đổi nhẹ để phân biệt
    val backgroundColor = if (comment.depth_level % 2 == 0) Color.White else Color(0xFFF8F9FA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(
                start = paddingLeft,
                top = 8.dp,
                bottom = 8.dp,
                end = 16.dp
            )
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            var avatar = when{
                comment?.author_avatar.isNullOrBlank() -> {
                    "https://ui-avatars.com/api/?name=${comment.username ?: "User"}&background=random&size=128"
                }else -> {
                    "$BASE_URL${comment?.author_avatar}"
                }
            }
            AsyncImage(
                model = avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tên user + Thời gian
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${comment.username}",
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
                    VoteActionPill(
                        upvotes = curUpvote.toString(),
                        downvotes = curDownvote.toString(),
                        curVote = commentVoteState,
                        onVote = { newType ->
                            val action = VoteHelper.resolveVote(commentVoteState, newType)
                            if (!action.shouldCallApi) return@VoteActionPill
                            when (newType) {
                                VoteType.upvote -> {
                                    curUpvote++
                                    if (commentVoteState == VoteType.downvote) curDownvote--
                                }
                                VoteType.downvote -> {
                                    curDownvote++
                                    if (commentVoteState == VoteType.upvote) curUpvote--
                                }
                                VoteType.none -> {
                                    if (commentVoteState == VoteType.upvote) curUpvote--
                                    if (commentVoteState == VoteType.downvote) curDownvote--
                                }
                            }
                            val oldVote = commentVoteState
                            commentVoteState = newType
                            scope.launch {
                                val success = voteController.postVote(
                                    token = token,
                                    target = "comment",
                                    id = comment.id,
                                    type = action.apiType!!
                                )
                                if (success){
                                    if (oldVote == VoteType.none && newType != VoteType.none){
                                        val receiverId = comment.user_id
                                        val senderId = UserManager.getUserId(context)
                                        if (receiverId != null && receiverId != senderId){
                                            val content = when (newType) {
                                                VoteType.upvote -> "${username} đã upvote bài viết của bạn"
                                                VoteType.downvote -> "${username} đã downvote bài viết của bạn"
                                                else -> return@launch
                                            }
                                            notificationController.postNotification(
                                                token = token,
                                                receiverId = receiverId,
                                                senderId = senderId,
                                                commentId = comment.id,
                                                postId = comment.post_id,
                                                type = "vote",
                                                content = content
                                            )
                                        }

                                    }
                                }
                            }
                        }
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
@Composable
fun VoteActionPill(
    upvotes: String,
    downvotes: String,
    curVote: VoteType,
    onVote: (VoteType) -> Unit
) {
    val colorOn = Color(0xFF0079D3)
    val colorUn = Color.Gray

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFFF0F0F0),
                RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // --- UPVOTE ---
        IconButton(
            onClick = {
                val newStatus = if (curVote == VoteType.upvote) VoteType.none else VoteType.upvote
                onVote(newStatus)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (curVote == VoteType.upvote) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Upvote",
                tint = if (curVote == VoteType.upvote) colorOn else colorUn,
                modifier = Modifier.size(20.dp)
            )
        }
        //
        Text(
            text = upvotes,
            fontWeight = FontWeight.Bold,
            color = if (curVote == VoteType.upvote) colorOn else colorUn,
            fontSize = 14.sp
        )
        //
        Spacer(modifier = Modifier.width(12.dp))
        //
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // --- DOWNVOTE ---
        IconButton(
            onClick = {
                val newStatus = if (curVote == VoteType.downvote) VoteType.none else VoteType.downvote
                onVote(newStatus)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (curVote == VoteType.downvote) Icons.Outlined.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Downvote",
                tint = if (curVote == VoteType.downvote) colorOn else colorUn,
                modifier = Modifier.size(20.dp)
            )
        }
        //
        Text(
            text = downvotes,
            fontWeight = FontWeight.Bold,
            color = if (curVote == VoteType.downvote) colorOn else colorUn,
            fontSize = 14.sp
        )
    }
}