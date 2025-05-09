package com.uillover.taskfire.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.uillover.taskfire.databinding.FragmentSignupBinding
import com.uillover.taskfire.model.User
import com.uillover.taskfire.utils.Validator

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = "Veuillez entrer votre nom"
                return@setOnClickListener
            }

            if (!Validator.validateEmail(email)) {
                binding.etEmail.error = "Email invalide"
                return@setOnClickListener
            }

            if (!Validator.validatePassword(password)) {
                binding.etPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.etConfirmPassword.error = "Les mots de passe ne correspondent pas"
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                        addUserToDatabase(userId, name, email)

                        Toast.makeText(requireContext(), "Inscription réussie!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(binding.root.id, LoginFragment())
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), "Échec de l'inscription: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun addUserToDatabase(userId: String, name: String, email: String) {
        val user = User(id = userId, name = name, email = email, role = "User")
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Utilisateur enregistré avec succès!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erreur d'ajout utilisateur: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
