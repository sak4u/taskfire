package com.uillover.taskfire.utils


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseUtils {
    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = Firebase.database
    val tasksRef: DatabaseReference = database.getReference("tasks")

    fun getCurrentUser() = auth.currentUser
    fun getCurrentUserId() = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
    }
}