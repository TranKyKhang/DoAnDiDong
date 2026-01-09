// ApiService.kt - Đảm bảo đúng như sau
package com.example.firebaseauthapp

import retrofit2.http.GET

interface ApiService {
    @GET("api/user")
    suspend fun getUsers(): UsersResponse
}

data class UsersResponse(
    val success: Boolean,
    val authenticated_uid: String,
    val users: List<User>
)

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val password_hash: String,
    val display_name: String?,
    val bio: String?,
    val avatar: String?,
    val banner: String?,
    val post_rating: Int,
    val comment_rating: Int,
    val created_at: String
)