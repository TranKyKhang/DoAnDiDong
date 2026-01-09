package com.example.appmangxahoi.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentFeed: String, // 1. Nhận tên Feed hiện tại (Home/Popular) từ HomeScreen
    onAvatarClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFeedClick: (String) -> Unit // 2. Sự kiện khi chọn menu
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        title = {
            Box(contentAlignment = Alignment.CenterStart) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = currentFeed, // 3. Hiển thị tên Feed hiện tại
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Select feed",
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    // Option 1: Home
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = {
                            expanded = false
                            onFeedClick("Home") // 4. Báo ra ngoài là chọn Home
                        },
                        leadingIcon = { Icon(Icons.Filled.Home, null) }
                    )

                    // Option 2: Popular
                    DropdownMenuItem(
                        text = { Text("Popular") },
                        onClick = {
                            expanded = false
                            onFeedClick("Popular") // 5. Báo ra ngoài là chọn Popular
                        },
                        leadingIcon = { Icon(Icons.Filled.Star, null) }
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = onAvatarClick) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
            }
        }
    )
}