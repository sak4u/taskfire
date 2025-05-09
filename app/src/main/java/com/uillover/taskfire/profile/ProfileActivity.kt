package com.uillover.taskfire.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uillover.taskfire.auth.AuthActivity
import com.uillover.taskfire.databinding.ActivityProfileBinding
import com.uillover.taskfire.utils.FirebaseUtils

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserData()
        setupListeners()
    }

    private fun setupToolbar() {
        // Cet appel ne posera plus de problème puisque le thème désactive l'ActionBar par défaut
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profil"
    }

    private fun loadUserData() {
        val user = FirebaseUtils.getCurrentUser()
        user?.let {
            binding.tvEmail.text = it.email
            binding.tvUserId.text = it.uid
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            FirebaseUtils.logout()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity()
        }

        binding.btnDeleteAccount.setOnClickListener {
            FirebaseUtils.getCurrentUser()?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AuthActivity::class.java))
                    finishAffinity()
                } else {
                    Toast.makeText(
                        this,
                        "Erreur: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
