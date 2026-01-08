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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// 1. Tạo Data Class đơn giản cho Cộng đồng (Subreddit)
data class Community(
    val name: String,
    val avatarUrl: String,
    val isFavorite: Boolean = false
)

// 2. Dữ liệu giả lập các cộng đồng đang follow
val followedCommunities = listOf(
    Community("r/androiddev", "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=100"),
    Community("r/kotlin", "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100"),
    Community("r/vietnam", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100"),
    Community("r/funny", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=100"),
    Community("r/technology", "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=100"),
    Community("r/pics", "https://images.unsplash.com/photo-1504194104404-433180773017?w=100")
)

@Composable
fun AppDrawer(
    onItemClick: (String) -> Unit = {},
    onCreateGroupClick: () -> Unit = {}
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(300.dp) // Giới hạn chiều rộng drawer cho đẹp
    ) {
        // --- Header của Drawer ---
        Column(modifier = Modifier.padding(16.dp)) {
            Text("App Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        HorizontalDivider()

        // --- Danh sách cuộn ---
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ){
            // Section 2: Cộng đồng của bạn
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
                    // Nút tạo cộng đồng mới (tùy chọn)
                    IconButton(
                        onClick = onCreateGroupClick, // Gọi hàm khi bấm
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Group", tint = Color.Gray)
                    }
                }
            }

            // Render danh sách dynamic từ List
            items(followedCommunities) { community ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = community.name,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    icon = {
                        // Hiển thị Avatar tròn
                        AsyncImage(
                            model = community.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp) // Kích thước chuẩn cho icon menu
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    },
                    selected = false, // Xử lý logic chọn sau này
                    onClick = {
                        onItemClick(community.name) // <--- TRUYỀN TÊN CỘNG ĐỒNG RA NGOÀI
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),

                )
            }
        }
    }
}