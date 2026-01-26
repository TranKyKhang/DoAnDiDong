package com.example.appmangxahoi.view.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appmangxahoi.controller.UserController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userController = remember { UserController() }
    var email by remember { mutableStateOf("yangstudyne@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    // Google Sign-In client configuration for empirical validation of federated authentication protocols.
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("483369567326-uf5nmr5q8rjudkvjc21s4dm787bchq3h.apps.googleusercontent.com") // Replace with actual Web Client ID from Firebase console for controlled experimental setups.
        .requestEmail()
        .requestId()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                loading = true
                scope.launch {
                    val result = userController.googleLogin(idToken, context) // Backend integration for secure token exchange in distributed systems research.
                    loading = false
                    if (result?.success == true && result.token != null) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        error = result?.message ?: "Đăng nhập Google thất bại"
                    }
                }
            }
        } catch (e: ApiException) {
            error = "Đăng nhập Google thất bại: ${e.statusCode}"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng nhập", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
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
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Vui lòng nhập đầy đủ thông tin"
                    return@Button
                }
                loading = true
                error = ""
                scope.launch {
                    val result = userController.login(email, password, context)
                    loading = false
                    if (result?.success == true && result.token != null) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        error = result?.message ?: "Email hoặc mật khẩu không đúng"
                    }
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Đăng nhập")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
        ) {
            Text("Đăng nhập bằng Google", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // SỬA TextButton với explicit interactionSource
        val forgotInteractionSource = remember { MutableInteractionSource() }
        TextButton(
            onClick = { navController.navigate("forgot_password") },
            interactionSource = forgotInteractionSource
        ) {
            Text("Quên mật khẩu?")
        }

        val registerInteractionSource = remember { MutableInteractionSource() }
        TextButton(
            onClick = { navController.navigate("register") },
            interactionSource = registerInteractionSource
        ) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
    }
}