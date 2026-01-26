package com.example.appmangxahoi.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onBackClick: () -> Unit,      // Hàm xử lý khi bấm nút Back
    onSettingsClick: () -> Unit   // Hàm xử lý khi bấm nút Cài đặt
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        ),
        // 1. Nút Back bên trái
        navigationIcon = {
            val backInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onBackClick,
                interactionSource = backInteraction
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        // 2. Tiêu đề
        title = {
            Text(
                text = "Trang cá nhân",
                fontWeight = FontWeight.Bold
            )
        },
        // 3. Nút Settings bên phải
        actions = {
            val settingsInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onSettingsClick,
                interactionSource = settingsInteraction
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}