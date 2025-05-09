package com.uillover.taskfire.tasks

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.uillover.taskfire.databinding.ItemTaskBinding
import com.uillover.taskfire.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private var tasks: List<Task>,
    private val userId: String, // Filtrer les tâches de l'utilisateur connecté
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.binding.apply {
            taskTitle.text = task.title
            taskDueDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(task.dueDate)

            when (task.priority) {
                "high" -> priorityIndicator.setBackgroundColor(Color.RED)
                "medium" -> priorityIndicator.setBackgroundColor(Color.YELLOW)
                else -> priorityIndicator.setBackgroundColor(Color.GREEN)
            }

            cbCompleted.setOnCheckedChangeListener(null) // Empêche un déclenchement involontaire du listener
            cbCompleted.isChecked = task.status == "completed" // Applique l'état correct
            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                updateTaskStatus(task, isChecked) // Met à jour Firebase
            }

                 assignedBy.text = if (task.assignedBy.isNotEmpty()) {
                "Assigné par : ${task.assignedBy}"
            } else {
                "Assigné par : Inconnu"
            }

            // Ajout d'un log pour vérifier la récupération des données
            Log.d("TaskAdapter", "Affichage de la tâche - ID: ${task.id}, Assigné par: ${task.assignedBy}")

            root.setOnClickListener { onTaskClick(task) }
            root.setOnLongClickListener {
                onTaskLongClick(task)
                true
            }
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks.filter { it.userId == userId }
            .sortedBy { it.status } // Trie les tâches par statut (`pending`, `in_progress`, `completed`)
        notifyDataSetChanged()
    }

    private fun updateTaskStatus(task: Task, isChecked: Boolean) {
        val updatedStatus = if (isChecked) "completed" else "in_progress"
        val dbRef = FirebaseDatabase.getInstance().reference.child("tasks").child(task.id)

        dbRef.updateChildren(mapOf("status" to updatedStatus)).addOnSuccessListener {
            Log.d("TaskAdapter", "Tâche ${task.id} mise à jour avec statut : $updatedStatus") // Log utile pour le débogage

            // Vérifie que l’élément existe avant de rafraîchir RecyclerView
            val index = tasks.indexOf(task)
            if (index >= 0) {
                notifyItemChanged(index)
            }
        }.addOnFailureListener {
            Log.e("TaskAdapter", "Erreur mise à jour Firebase : ${it.message}")
        }
    }
}
