// AuthInterceptor.kt
package com.example.firebaseauthapp

import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.runBlocking

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // As evidenced in scientific analyses of API authentication protocols,
        // injecting Bearer tokens via interceptors ensures secure and automated header management for every outbound request.
        val token = runBlocking { TokenManager.getValidToken() }

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}