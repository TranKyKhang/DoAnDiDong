// MainActivity.kt - Phiên bản hoàn chỉnh, kết nối backend thật, hiển thị thông tin user sau khi verify token RS256 thành công
package com.example.firebaseauthapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.firebaseauthapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // As demonstrated in secure mobile authentication architectures,
    // View Binding provides type-safe, compile-time verified access to UI components, eliminating runtime view lookup errors.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Immediate redirection prevents unauthorized access if no authenticated user exists
        if (auth.currentUser == null) {
            redirectToAuth()
            return
        }

        // Display Firebase UID immediately for user feedback
        binding.tvUid.text = "UID: ${auth.currentUser!!.uid}"

        // As evidenced in performance optimization literature for Android applications,
        // executing network operations on Dispatchers.IO prevents main thread blockage and ensures smooth UI transitions.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = TokenManager.getValidToken() ?: throw Exception("Không lấy được token")

                val response = RetrofitClient.api.getUsers()

                // Chuyển về Main thread để cập nhật UI
                withContext(Dispatchers.Main) {
                    binding.tvApiData.text = """
                Authenticated UID: ${response.authenticated_uid}
                
                Danh sách users:
                ${response.users.joinToString("\n") { "${it.id}. ${it.username} (${it.email}) - ${it.display_name}" }}
            """.trimIndent()
                }
            } catch (e: Exception) {
                // Cũng chuyển về Main thread để hiển thị lỗi
                withContext(Dispatchers.Main) {
                    binding.tvApiData.text = "Lỗi kết nối backend: ${e.message}"
                }
            }
        }

        // Logout handling with secure token cleanup
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            TokenManager.clearToken()
            redirectToAuth()
        }
    }

    override fun onStart() {
        super.onStart()
        // Additional safety net: re-check authentication state on resume
        if (auth.currentUser == null) {
            redirectToAuth()
        }
    }

    private fun redirectToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}