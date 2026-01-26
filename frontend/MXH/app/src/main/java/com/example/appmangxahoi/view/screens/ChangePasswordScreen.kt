package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.appmangxahoi.controller.UserController
import com.example.appmangxahoi.utils.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    rootNavController: NavHostController,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userController = remember { UserController() }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF2F4F8))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Mật khẩu hiện tại", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Mật khẩu mới", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Xác nhận mật khẩu mới", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = confirmPass.isNotEmpty() && confirmPass != newPass,
                        supportingText = {
                            if (confirmPass.isNotEmpty() && confirmPass != newPass) {
                                Text("Mật khẩu xác nhận không khớp", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() -> {
                            error = "Vui lòng nhập đầy đủ thông tin"
                        }
                        newPass != confirmPass -> {
                            error = "Mật khẩu mới không khớp xác nhận"
                        }
                        newPass.length < 6 -> {
                            error = "Mật khẩu mới phải có ít nhất 6 ký tự"
                        }
                        else -> {
                            error = ""
                            showConfirmDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079D3)),
                enabled = !loading &&
                        oldPass.isNotBlank() &&
                        newPass.isNotBlank() &&
                        confirmPass.isNotBlank() &&
                        newPass == confirmPass
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Cập nhật mật khẩu", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (successMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // Dialog xác nhận
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận đổi mật khẩu") },
            text = { Text("Bạn có chắc chắn muốn đổi mật khẩu? Sau khi đổi thành công, bạn sẽ phải đăng nhập lại.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        loading = true
                        error = ""
                        successMessage = ""

                        scope.launch {
                            val success = userController.changePassword(oldPass, newPass, context)

                            loading = false

                            if (success) {
                                successMessage = "Đổi mật khẩu thành công! Đang chuyển về trang đăng nhập..."
                                TokenManager.clearToken(context)
                                delay(2000)
                                rootNavController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                error = "Đổi mật khẩu thất bại. Kiểm tra lại mật khẩu cũ hoặc kết nối mạng."
                            }
                        }
                    }
                ) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Hủy") }
            }
        )
    }
}