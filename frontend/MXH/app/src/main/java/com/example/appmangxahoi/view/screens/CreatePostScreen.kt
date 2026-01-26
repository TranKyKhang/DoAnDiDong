package com.example.appmangxahoi.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ripple
import coil.compose.AsyncImage
import com.example.appmangxahoi.controller.community
import com.example.appmangxahoi.controller.post
import com.example.appmangxahoi.model.CommunityModel
import com.example.appmangxahoi.utils.UserManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onContentChange: (Boolean) -> Unit,
    onPostSuccess: () -> Unit,
    onPostFail: (String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val communityController = remember { community() }
    val postController = remember { post() }
    var communities by remember { mutableStateOf<List<CommunityModel>>(emptyList()) }
    var selectedCommunity by remember { mutableStateOf<CommunityModel?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var linkUrl by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var showCommunitySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Load communities
    LaunchedEffect(Unit) {
        communities = communityController.getMyCommunity(context) ?: emptyList()
    }

    // Enable submit button
    LaunchedEffect(title, imageUris, videoUri, linkUrl) {
        onContentChange(
            title.isNotBlank() ||
                    imageUris.isNotEmpty() ||
                    videoUri != null ||
                    linkUrl.isNotBlank()
        )
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        imageUris = uris
        videoUri = null
        linkUrl = ""
    }

    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        videoUri = uri
        imageUris = emptyList()
        linkUrl = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Community selector
        val communityInteraction = remember { MutableInteractionSource() }
        Surface(
            onClick = { showCommunitySheet = true },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF2F2F2),
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable(
                        interactionSource = communityInteraction,
                        indication = ripple(),
                        onClick = { showCommunitySheet = true }
                    )
            ) {
                if (selectedCommunity?.icon != null) {
                    AsyncImage(
                        model = "http://10.0.2.2:3000${selectedCommunity!!.icon}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedCommunity?.name ?: "",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, null)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Title
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Tiêu đề", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        // Body
        TextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text("Nội dung bài viết") },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        // Link preview
        if (linkUrl.isNotBlank()) {
            Text(
                "🔗 $linkUrl",
                color = Color(0xFF0079D3),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Image preview
        if (imageUris.isNotEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                imageUris.forEach { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Video preview
        videoUri?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        HorizontalDivider()

        // Attach bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val imageInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                interactionSource = imageInteraction
            ) {
                Icon(Icons.Outlined.Image, null)
            }

            val videoInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                interactionSource = videoInteraction
            ) {
                Icon(Icons.Outlined.VideoLibrary, null)
            }

            val linkInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    showLinkDialog = true
                    imageUris = emptyList()
                    videoUri = null
                },
                interactionSource = linkInteraction
            ) {
                Icon(Icons.Default.Link, null)
            }
        }

        // Submit button
        Button(
            onClick = {
                scope.launch {
                    submitting = true
                    val success = postController.createPost(
                        context = context,
                        title = title,
                        content = body,
                        communityId = selectedCommunity?.id,
                        imageUris = imageUris,
                        videoUri = videoUri,
                        linkUrl = linkUrl
                    )
                    submitting = false
                    if (success) {
                        onPostSuccess()
                        // Reset form
                        title = ""
                        body = ""
                        imageUris = emptyList()
                        videoUri = null
                        linkUrl = ""
                        selectedCommunity = null
                    } else {
                        onPostFail("Đăng bài thất bại. Vui lòng thử lại")
                    }
                }
            },
            enabled = title.isNotBlank() && !submitting && selectedCommunity != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079D3))
        ) {
            Text("Đăng bài", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    // Link dialog
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Nhập link") },
            text = {
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    placeholder = { Text("https://...") }
                )
            },
            confirmButton = {
                TextButton(onClick = { showLinkDialog = false }) { Text("OK") }
            }
        )
    }

    // Community bottom sheet
    if (showCommunitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommunitySheet = false },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(communities) { community ->
                    val itemInteraction = remember { MutableInteractionSource() }
                    ListItem(
                        leadingContent = {
                            if (community.icon != null) {
                                AsyncImage(
                                    model = "http://10.0.2.2:3000${community.icon}",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Public, null)
                            }
                        },
                        headlineContent = { Text(community.name) },
                        modifier = Modifier.clickable(
                            interactionSource = itemInteraction,
                            indication = ripple(),
                            onClick = {
                                scope.launch {
                                    // Lấy danh sách thành viên để kiểm tra trạng thái
                                    val members = communityController.getMembersByCommunityID(context, community.id)
                                    val myId = UserManager.getUserId(context)

                                    // Tìm bản thân mình và xem có bị cấm (isBanned == 1) không
                                    val isBanned = members.find { it.id == myId }?.isBanned == 1

                                    if (isBanned) {
                                        // Nếu bị cấm: Hiện thông báo và KHÔNG cho chọn
                                        Toast.makeText(context, "Bạn đã bị cấm đăng bài trong nhóm này", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Nếu không bị cấm: Cho chọn bình thường
                                        selectedCommunity = community
                                        showCommunitySheet = false
                                    }
                                }
                            }
                        )
                    )
                }
            }
        }
    }
}