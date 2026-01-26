package com.example.appmangxahoi.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.wear.compose.material.ripple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentFeed: String,
    onAvatarClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFeedClick: (String) -> Unit
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
                val feedInteraction = remember { MutableInteractionSource() }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            interactionSource = feedInteraction,
                            indication = ripple(),
                            onClick = { expanded = !expanded }
                        )
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = currentFeed,
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
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = {
                            expanded = false
                            onFeedClick("Home")
                        },
                        leadingIcon = { Icon(Icons.Filled.Home, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Popular") },
                        onClick = {
                            expanded = false
                            onFeedClick("Popular")
                        },
                        leadingIcon = { Icon(Icons.Filled.Star, null) }
                    )
                }
            }
        },
        actions = {
            // Nút Search - FIX CLICKABLE
            val searchInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onSearchClick,
                interactionSource = searchInteraction
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }

            // Nút Profile - FIX CLICKABLE
            val avatarInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onAvatarClick,
                interactionSource = avatarInteraction
            ) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
            }
        }
    )
}