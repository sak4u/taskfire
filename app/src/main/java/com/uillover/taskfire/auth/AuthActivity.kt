package com.uillover.taskfire.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uillover.taskfire.databinding.ActivityAuthBinding
import com.uillover.taskfire.tasks.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(binding.authContainer.id, LoginFragment())
                .commit()
        }

        binding.btnSignup.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(binding.authContainer.id, SignupFragment())
                .commit()
        }
    }
}