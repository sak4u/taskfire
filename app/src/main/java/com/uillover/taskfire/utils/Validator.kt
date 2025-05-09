package com.uillover.taskfire.utils



object Validator {
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateTaskTitle(title: String): Boolean {
        return title.trim().isNotEmpty()
    }
}