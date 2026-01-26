package com.example.appmangxahoi.view.component

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.appmangxahoi.view.screens.*

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController() // Root NavController
    NavHost(
        navController = rootNavController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(rootNavController) }
        composable("register") { RegisterScreen(rootNavController) }
        composable("forgot_password") { ForgotPasswordScreen(navController = rootNavController) }
        composable("home") {
            AppHomeScreen(rootNavController = rootNavController)
        }
    }
}