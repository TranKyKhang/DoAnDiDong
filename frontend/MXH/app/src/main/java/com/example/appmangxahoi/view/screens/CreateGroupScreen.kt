
package com.example.appmangxahoi.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ripple
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class CreateCommunityData(
    val name: String,
    val description: String,
    val rules: String,
    val iconUri: Uri?,
    val bannerUri: Uri?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onDismiss: () -> Unit,
    onCreate: (CreateCommunityData) -> Unit
) {
    val context = LocalContext.current
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var selectedBannerUri by remember { mutableStateOf<Uri?>(null) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedBannerUri = uri
    }
    val iconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedIconUri = uri
    }
    val isValid = groupName.length >= 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo cộng đồng", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onCreate(
                                CreateCommunityData(
                                    name = groupName,
                                    description = description,
                                    rules = rules,
                                    iconUri = selectedIconUri,
                                    bannerUri = selectedBannerUri
                                )
                            )
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0079D3),
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
                    ) {
                        Text("Tạo", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val bannerInteractionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFFE0E0E0))
                            .clickable(
                                interactionSource = bannerInteractionSource,
                                indication = ripple(),
                                onClick = {
                                    bannerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBannerUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(selectedBannerUri).build(),
                                contentDescription = "Banner Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                Text("Chạm để thêm ảnh bìa", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                    val iconInteractionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(80.dp)
                            .align(Alignment.BottomStart)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .background(Color(0xFFBDBDBD))
                            .clickable(
                                interactionSource = iconInteractionSource,
                                indication = ripple(),
                                onClick = {
                                    iconLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedIconUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(selectedIconUri).build(),
                                contentDescription = "Icon Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                Text(
                    "Bấm vào hình tròn hoặc hình chữ nhật để tải ảnh lên",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 8.dp)
                )
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tên cộng đồng *", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text("VD: laptrinhvien") },
                        singleLine = true,
                        prefix = { Text("r/", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Mô tả", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Cộng đồng này thảo luận về vấn đề gì?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Nội quy (Rules)", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rules,
                        onValueChange = { rules = it },
                        placeholder = { Text("1. Không spam\n2. Tôn trọng lẫn nhau...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 32.dp)
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}