    package com.example.appmangxahoi.view.component

    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.fillMaxHeight
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material3.HorizontalDivider
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.ModalDrawerSheet
    import androidx.compose.material3.NavigationDrawerItem
    import androidx.compose.material3.NavigationDrawerItemDefaults
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import coil.compose.AsyncImage
    import com.example.appmangxahoi.controller.community
    import com.example.appmangxahoi.model.CommunityModel

    // 1. Tạo Data Class đơn giản cho Cộng đồng (Subreddit)


    @Composable
    fun AppDrawer(
        onItemClick: (Int) -> Unit = {

        },
        onCreateGroupClick: () -> Unit = {}
    ) {
        val context = LocalContext.current
        val controller = remember { community() }
        var communities by remember { mutableStateOf<List<CommunityModel>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            loading = true
            communities = controller.getMyCommunity(context) ?: emptyList()
            loading = false
        }

        ModalDrawerSheet(
            drawerContainerColor = Color.White,
            modifier = Modifier.width(300.dp)
        ) {

            // ===== HEADER =====
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            HorizontalDivider()

            // ===== LIST =====
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {

                // ===== TITLE =====
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 12.dp, top = 24.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CỘNG ĐỒNG CỦA BẠN",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        IconButton(
                            onClick = onCreateGroupClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create Group",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // ===== LOADING =====
                if (loading) {
                    item {
                        Text(
                            "Đang tải...",
                            modifier = Modifier.padding(24.dp),
                            color = Color.Gray
                        )
                    }
                }

                // ===== COMMUNITY ITEMS =====
                items(communities) { community ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = "r/${community.name}",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        icon = {
                            AsyncImage(
                                model ="http://10.0.2.2:3000${community.icon}"
                                    ?: "https://ui-avatars.com/api/?name=${community.name}",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                        },
                        selected = false,
                        onClick = {
                            onItemClick(community.id)
                        },
                        modifier = Modifier.padding(
                            vertical = 6.dp, horizontal = 10.dp
                        )
                    )
                }
            }
        }
    }
