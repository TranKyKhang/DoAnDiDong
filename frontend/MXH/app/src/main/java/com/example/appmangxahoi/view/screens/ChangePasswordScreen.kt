package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

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
                .background(Color(0xFFF2F4F8)) // Nền xám nhạt đồng bộ
                .padding(16.dp)
        ) {
            // Card chứa form nhập liệu
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Mật khẩu hiện tại", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(), // Ẩn pass thành dấu *
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

                    Text("Nhập lại mật khẩu mới", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = confirmPass.isNotEmpty() && confirmPass != newPass // Báo đỏ nếu không khớp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Lưu thay đổi
            Button(
                onClick = {
                    // TODO: Gọi API đổi pass tại đây
                    onBackClick() // Tạm thời bấm xong thì quay lại
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0079D3)),
                enabled = oldPass.isNotEmpty() && newPass.isNotEmpty() && newPass == confirmPass
            ) {
                Text("Cập nhật mật khẩu", fontWeight = FontWeight.Bold)
            }
        }
    }
}