// AuthActivity.kt - Sửa lỗi "source must not be null" do ID view sai trong XML
package com.example.firebaseauthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseauthapp.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.firebaseauthapp.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // As demonstrated in modern Android development practices,
    // View Binding provides compile-time safety for view references, preventing runtime errors associated with incorrect IDs.
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ID phải đúng với XML: btn_register → btnRegister, et_email → etEmail, etc.
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                binding.tvMessage.text = "Vui lòng nhập đầy đủ email và mật khẩu"
                return@setOnClickListener
            }
            registerUser(email, password)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                binding.tvMessage.text = "Vui lòng nhập đầy đủ email và mật khẩu"
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                } else {
                    binding.tvMessage.text = task.exception?.message ?: "Đăng ký thất bại"
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.tvMessage.text = task.exception?.message ?: "Đăng nhập thất bại"
                }
            }
    }
}