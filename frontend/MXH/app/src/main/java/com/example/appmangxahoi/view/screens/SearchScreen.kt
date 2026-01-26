package com.example.appmangxahoi.view.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.appmangxahoi.model.mockPosts
import com.example.appmangxahoi.view.component.AppPostItem
import android.util.Log
import androidx.wear.compose.material.ripple

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onPostClick: (String) -> Unit = {},
    targetCommunity: String? = null
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val targetDisplayName = remember(targetCommunity) { targetCommunity?.replace("_", "/") }
    val filteredPosts = remember(query, targetDisplayName) {
        if (query.isBlank()) emptyList()
        else mockPosts.filter { post ->
            val matchesQuery = post.title.contains(query, ignoreCase = true) ||
                    post.authorName.contains(query, ignoreCase = true) ||
                    post.communityName.contains(query, ignoreCase = true)
            val matchesCommunity = if (targetDisplayName != null) {
                post.communityName == targetDisplayName
            } else true
            matchesQuery && matchesCommunity
        }
    }

    // Speech-to-Text state
    var isRecording by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            permissionGranted = true
        }
        focusRequester.requestFocus()
    }

    // Recognition listener with improved handling and logging
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isRecording = true
                Log.d("Speech", "Ready for speech")
            }
            override fun onBeginningOfSpeech() {
                Log.d("Speech", "Beginning of speech")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isRecording = false
                Log.d("Speech", "End of speech")
            }
            override fun onError(error: Int) {
                isRecording = false
                Log.e("Speech", "Error: $error")  // Log lỗi để debug (ví dụ error 9: insufficient permissions)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("Speech", "Full results: $matches")
                if (!matches.isNullOrEmpty()) {
                    query = matches[0]  // Lấy kết quả đầu tiên
                } else {
                    query = "No speech detected"  // Fallback nếu không nhận diện
                }
                isRecording = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("Speech", "Partial results: $partial")
                if (!partial.isNullOrEmpty()) {
                    query = partial[0]  // Cập nhật realtime từ partial results
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(if (targetDisplayName != null) "Tìm trong $targetDisplayName" else "Tìm kiếm...")
                    },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }

                // Nút micro - Nhấn giữ để ghi âm
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Press -> {
                                            if (permissionGranted) {
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                                                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // Bật partial results cho realtime
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói để tìm kiếm...")
                                                }
                                                speechRecognizer.setRecognitionListener(recognitionListener)
                                                speechRecognizer.startListening(intent)
                                                Log.d("Speech", "Start listening")
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                        PointerEventType.Release -> {
                                            speechRecognizer.stopListening()
                                            Log.d("Speech", "Stop listening")
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Ghi âm",
                        tint = if (isRecording) Color.Red else Color.Gray
                    )
                }
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (query.isBlank()) {
                    item {
                        Text("Tìm kiếm gần đây", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    }
                    val historyItems = listOf("Kotlin", "Android", "Vietnam")
                    items(historyItems) { historyItem ->
                        val historyInteraction = remember { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = historyInteraction,
                                    indication = ripple(),
                                    onClick = { query = historyItem }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(historyItem)
                        }
                    }
                } else if (filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không tìm thấy kết quả nào", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredPosts) { post ->
                        AppPostItem(post = post)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }
}