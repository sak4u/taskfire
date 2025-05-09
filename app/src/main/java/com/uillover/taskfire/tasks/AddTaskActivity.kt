package com.uillover.taskfire.tasks

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.uillover.taskfire.databinding.ActivityAddTaskBinding
import com.uillover.taskfire.model.Task
import java.util.Calendar
import java.util.UUID

class AddTaskActivity : AppCompatActivity() {

    private var _binding: ActivityAddTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUsers() // Charge la liste des utilisateurs dans le Spinner
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nouvelle Tâche"
    }

    private fun setupSaveButton() {
        binding.btnSaveTask.setOnClickListener {
            saveTaskToFirebase()
        }
    }

    private fun loadUsers() {
        val userList = mutableListOf<String>()
        FirebaseDatabase.getInstance().reference.child("users")
            .get()
            .addOnSuccessListener { snapshot ->
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val userName = userSnapshot.child("name").value.toString()
                    userList.add("$userName - $userId")
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spUserId.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur lors du chargement des utilisateurs", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTaskToFirebase() {
        Log.d("AddTaskActivity", "Méthode saveTaskToFirebase() appelée")
        Toast.makeText(this, "Sauvegarde en cours...", Toast.LENGTH_SHORT).show()

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            Log.e("AddTaskActivity", "Titre manquant, annulation de la sauvegarde")
            binding.etTitle.error = "Titre requis"
            return
        }

        if (binding.spUserId.selectedItem == null) {
            Log.e("AddTaskActivity", "Utilisateur assigné manquant, annulation de la sauvegarde")
            Toast.makeText(this, "Veuillez sélectionner un utilisateur", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = binding.spUserId.selectedItem.toString().split(" - ").last().trim()

        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            binding.rbHigh.id -> "high"
            binding.rbMedium.id -> "medium"
            else -> "low"
        }

        val calendar = Calendar.getInstance().apply {
            set(binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth)
        }

        val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // ID de l'admin connecté

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            dueDate = calendar.timeInMillis,
            userId = userId, // ID du User
            assignedBy = adminId, // ID de l'Admin
            status = "pending" // Statut initial
        )

        Log.d("AddTaskActivity", "Tâche créée : $task")

        FirebaseDatabase.getInstance().reference
            .child("tasks")
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Log.d("AddTaskActivity", "Tâche sauvegardée avec succès")
                    Toast.makeText(this, "Tâche assignée", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("AddTaskActivity", "Erreur Firebase : ${dbTask.exception?.message}")
                    Toast.makeText(this, "Erreur: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
