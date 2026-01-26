package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appmangxahoi.controller.UserController
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userController = remember { UserController() }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: gửi email, 2: nhập OTP + pass mới
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (step == 1) "Quên mật khẩu" else "Đặt lại mật khẩu",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(32.dp))
        if (step == 1) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isBlank()) {
                        error = "Vui lòng nhập email"
                        return@Button
                    }
                    loading = true
                    error = ""
                    message = ""
                    scope.launch {
                        // Gọi API forgot password (backend gửi OTP)
                        val success = userController.forgotPassword(email)
                        loading = false
                        if (success) {
                            message = "Đã gửi OTP đến email của bạn"
                            step = 2
                        } else {
                            error = "Không tìm thấy email hoặc lỗi gửi OTP"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Gửi OTP")
            }
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Mã OTP") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Mật khẩu mới") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Xác nhận mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            if (message.isNotEmpty()) Text(message, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    when {
                        otp.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                            error = "Vui lòng nhập đầy đủ"
                            return@Button
                        }
                        newPassword != confirmPassword -> {
                            error = "Mật khẩu không khớp"
                            return@Button
                        }
                    }
                    loading = true
                    error = ""
                    scope.launch {
                        val success = userController.verifyResetOtp(email, otp, newPassword)
                        loading = false
                        if (success) {
                            message = "Đặt lại mật khẩu thành công"
                            navController.navigate("login") {
                                popUpTo("forgot_password") { inclusive = true }
                            }
                        } else {
                            error = "OTP không đúng hoặc hết hạn"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Đặt lại mật khẩu")
            }
        }
        Spacer(Modifier.height(16.dp))
        val loginInteractionSource = remember { MutableInteractionSource() }
        TextButton(
            onClick = { navController.navigate("login") },
            interactionSource = loginInteractionSource
        ) {
            Text("Quay lại đăng nhập")
        }
    }
}