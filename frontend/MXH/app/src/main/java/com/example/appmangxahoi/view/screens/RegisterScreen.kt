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
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userController = remember { UserController() }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng ký", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Tên người dùng") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when {
                    email.isBlank() || username.isBlank() || password.isBlank() -> {
                        error = "Vui lòng nhập đầy đủ thông tin"
                        return@Button
                    }
                    password != confirmPassword -> {
                        error = "Mật khẩu không khớp"
                        return@Button
                    }
                }
                loading = true
                error = ""
                scope.launch {
                    val result = userController.register(email, username, password, context) // Truyền context
                    loading = false
                    if (result?.success == true) {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        error = result?.message ?: "Đăng ký thất bại"
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Đăng ký")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // SỬA TextButton
        val loginInteractionSource = remember { MutableInteractionSource() }
        TextButton(
            onClick = { navController.navigate("login") },
            interactionSource = loginInteractionSource
        ) {
            Text("Đã có tài khoản? Đăng nhập ngay")
        }
    }
}