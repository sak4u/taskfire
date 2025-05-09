package com.uillover.taskfire.tasks

import android.os.Bundle
import android.widget.Toast
import com.uillover.taskfire.R
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.uillover.taskfire.databinding.ActivityTaskDetailBinding
import com.uillover.taskfire.model.Task
import java.util.Calendar

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurer la toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Détails de la tâche"

        // Récupérer l'ID de la tâche envoyée depuis MainActivity
        taskId = intent.getStringExtra("task_id")
        if (taskId == null) {
            Toast.makeText(this, "Erreur : ID de tâche manquant", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTaskDetails() // Charger les données de la tâche

        binding.btnUpdateTask.setOnClickListener {
            updateTask()
        }

        binding.btnDeleteTask.setOnClickListener {
            deleteTask()
        }
    }

    private fun loadTaskDetails() {
        val dbRef = FirebaseDatabase.getInstance().reference

        // D'abord chercher dans "tasks"
        dbRef.child("tasks").child(taskId!!)
            .get()
            .addOnSuccessListener { snapshot ->
                val task = snapshot.getValue(Task::class.java)
                if (task != null) {
                    displayTaskDetails(task) // Afficher la tâche trouvée
                } else {
                    // Si non trouvée dans "tasks", chercher dans "completed_tasks"
                    dbRef.child("completed_tasks").child(taskId!!)
                        .get()
                        .addOnSuccessListener { completedSnapshot ->
                            val completedTask = completedSnapshot.getValue(Task::class.java)
                            if (completedTask != null) {
                                displayTaskDetails(completedTask) // Afficher la tâche complétée trouvée
                            } else {
                                Toast.makeText(this, "Tâche introuvable", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de récupération : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fonction pour afficher les détails de la tâche
    private fun displayTaskDetails(task: Task) {
        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.description)

        binding.rgPriority.check(
            when (task.priority) {
                "high" -> R.id.rbHigh
                "medium" -> R.id.rbMedium
                else -> R.id.rbLow
            }
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = task.dueDate
        binding.tvDate.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"

        // Mettre à jour l'état du checkbox pour une tâche complétée
        binding.cbCompleted.isChecked = task.completed
    }


    private fun updateTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Titre requis"
            return
        }

        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            R.id.rbHigh -> "high"
            R.id.rbMedium -> "medium"
            else -> "low"
        }

        val completed = binding.cbCompleted.isChecked // Vérification du statut complété

        val updatedTask = mapOf(
            "title" to title,
            "description" to description,
            "priority" to priority,
            "completed" to completed
        )

        FirebaseDatabase.getInstance().reference.child("tasks").child(taskId!!)
            .updateChildren(updatedTask)
            .addOnSuccessListener {
                Toast.makeText(this, "Tâche mise à jour", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Échec de la mise à jour : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask() {
        FirebaseDatabase.getInstance().reference.child("tasks").child(taskId!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Échec de la suppression : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
