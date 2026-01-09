package com.example.appmangxahoi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // Tên group phải có ít nhất 3 ký tự mới cho tạo
    val isValid = groupName.length >= 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo cộng đồng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = { onCreate(groupName) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0079D3),
                            disabledContainerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Tạo", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("Tên cộng đồng", fontWeight = FontWeight.Medium, color = Color.Gray)
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("Ten_cong_dong") },
                singleLine = true,
                prefix = { Text("r/", fontWeight = FontWeight.Bold) }, // Tự động hiện chữ r/
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                "Tên không thể thay đổi sau khi tạo.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Text("Mô tả", fontWeight = FontWeight.Medium, color = Color.Gray)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Cộng đồng này về cái gì?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}