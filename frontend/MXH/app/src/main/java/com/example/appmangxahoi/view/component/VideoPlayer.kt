package com.example.appmangxahoi.view.component


import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 1. Tạo ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Tự động phát khi load xong
            playWhenReady = true
        }
    }

    // 2. Load Video từ link
    DisposableEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        // Quan trọng: Khi lướt đi chỗ khác thì tắt player để giải phóng bộ nhớ
        onDispose {
            exoPlayer.release()
        }
    }

    // 3. Hiển thị giao diện Player (Dùng AndroidView vì Compose chưa có VideoPlayer xịn)
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
            }
        },
        modifier = modifier
    )
}