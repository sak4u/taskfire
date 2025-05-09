package com.uillover.taskfire.tasks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uillover.taskfire.model.Task
import com.uillover.taskfire.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val userId = FirebaseUtils.getCurrentUserId() ?: return

        FirebaseUtils.tasksRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksList = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        taskSnapshot.getValue(Task::class.java)?.let { tasksList.add(it) }
                    }
                    _tasks.value = tasksList
                    Log.d("TaskViewModel", "Tâches chargées : ${tasksList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TaskViewModel", "Erreur Firebase : ${error.message}")
                }
            })
    }

    private val _completedTasks = MutableLiveData<List<Task>>()
    val completedTasks: LiveData<List<Task>> = _completedTasks

    fun loadCompletedTasks() {
        FirebaseUtils.tasksRef.orderByChild("status").equalTo("completed")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("TaskViewModel", "Chargement des tâches complétées... Nombre de tâches : ${snapshot.childrenCount}")
                    val tasksList = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null) {
                            tasksList.add(task)
                            Log.d("TaskViewModel", "Tâche complétée récupérée : ${task.title}, Assigné par: ${task.assignedBy}")
                        }
                    }
                    _completedTasks.value = tasksList
                    Log.d("TaskViewModel", "Total des tâches complétées : ${tasksList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TaskViewModel", "Erreur Firebase : ${error.message}")
                }
            })
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            FirebaseUtils.tasksRef.child(task.id).setValue(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val updatedValues = mapOf(
                "title" to task.title,
                "description" to task.description,
                "priority" to task.priority,
                "status" to task.status,
                "assignedBy" to task.assignedBy // Vérifie que cet attribut est bien mis à jour
            )

            FirebaseUtils.tasksRef.child(task.id).updateChildren(updatedValues)
                .addOnSuccessListener {
                    Log.d("TaskViewModel", "Tâche mise à jour avec succès : ${task.title}, Statut : ${task.status}")
                }
                .addOnFailureListener {
                    Log.e("TaskViewModel", "Échec de la mise à jour : ${it.message}")
                }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val userId = FirebaseUtils.getCurrentUserId()

            if (task.assignedBy == userId || userId == task.userId) { // Vérifie si l'admin ou l'utilisateur peut supprimer
                FirebaseUtils.tasksRef.child(task.id).removeValue()
                    .addOnSuccessListener {
                        Log.d("TaskViewModel", "Tâche supprimée : ${task.title}")
                    }
                    .addOnFailureListener {
                        Log.e("TaskViewModel", "Échec de la suppression : ${it.message}")
                    }
            } else {
                Log.e("TaskViewModel", "Suppression interdite : Seul l'assignateur ou l'utilisateur peut supprimer.")
            }
        }
    }
}
