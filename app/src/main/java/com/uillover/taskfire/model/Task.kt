package com.uillover.taskfire.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val priority: String = "low", // low, medium, high
    val completed: Boolean = false,
    val status: String = "pending", // pending, in_progress, completed
    val userId: String = "", // ID de l'utilisateur affecté
    val assignedBy: String = "", // ID de l'admin qui a créé la tâche
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis() // Dernière modification
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "dueDate" to dueDate,
            "priority" to priority,
            "completed" to completed,
            "status" to status,
            "userId" to userId,
            "assignedBy" to assignedBy,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
