package com.example.appmangxahoi

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.appmangxahoi.view.screens.AppHomeScreen
import com.example.appmangxahoi.view.screens.PostDetailScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    AppHomeScreen()
//                }
//            }
//        }
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Tạm thời ẩn màn hình chính đi
                    // AppHomeScreen()

                    // Gọi trực tiếp màn hình chi tiết với ID = 1
                    PostDetailScreen(
                        postId = 1,
                        onBackClick = { /* Không cần làm gì khi test cứng */ }
                    )
                }
            }
        }
    }
}