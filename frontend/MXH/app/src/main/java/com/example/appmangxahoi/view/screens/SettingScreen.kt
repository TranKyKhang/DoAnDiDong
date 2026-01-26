package com.example.appmangxahoi.view.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.ripple
import com.example.appmangxahoi.controller.UserController
import com.example.appmangxahoi.model.UserModel
import com.example.appmangxahoi.utils.UserManager
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    rootNavController: NavHostController,
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    user: UserModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loginMethod by remember { mutableStateOf("email") }
    var isLoadingMethod by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // 1. Ưu tiên dùng user truyền vào (từ Profile)
        if (user != null) {
            loginMethod = user.loginMethod ?: "email"
            isLoadingMethod = false
            println("SettingScreen: loginMethod từ user truyền vào = $loginMethod")
            return@LaunchedEffect
        }

        // 2. Nếu không có user truyền vào → lấy từ UserManager
        val savedMethod = UserManager.getLoginMethod(context)
        if (savedMethod != "email") {
            loginMethod = savedMethod
            isLoadingMethod = false
            println("SettingScreen: loginMethod từ SharedPreferences = $loginMethod")
            return@LaunchedEffect
        }

        // 3. Nếu chưa có → lấy từ profile API
        val profile = UserController().getMyProfile(context)
        loginMethod = profile?.loginMethod ?: "email"
        UserManager.saveLoginMethod(context, loginMethod)  // lưu để lần sau nhanh
        isLoadingMethod = false
        println("SettingScreen: loginMethod từ API profile = $loginMethod")
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F8),
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoadingMethod) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                SettingItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    onClick = {
                        if (loginMethod == "google") {
                            Toast.makeText(
                                context,
                                "Không hỗ trợ đổi mật khẩu cho tài khoản đăng nhập bằng Google",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onChangePasswordClick()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(2.dp))

                SettingItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Đăng xuất",
                    titleColor = Color.Red,
                    iconColor = Color.Red,
                    showArrow = false,
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Black,
    iconColor: Color = Color.Gray,
    showArrow: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )

            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}