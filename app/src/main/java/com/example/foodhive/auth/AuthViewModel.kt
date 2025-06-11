package com.example.foodhive.auth

import android.app.Application
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val fullName = mutableStateOf("")

    val emailError = mutableStateOf<String?>(null)
    val passwordError = mutableStateOf<String?>(null)
    val confirmPasswordError = mutableStateOf<String?>(null)
    val loading = mutableStateOf(false)

    fun validateEmail(): Boolean {
        return if (email.value.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            emailError.value = "Invalid email format"
            false
        } else {
            emailError.value = null
            true
        }
    }

    fun validatePassword(): Boolean {
        return if (password.value.length < 6) {
            passwordError.value = "Password must be at least 6 characters"
            false
        } else {
            passwordError.value = null
            true
        }
    }

    fun validateConfirmPassword(): Boolean {
        return if (confirmPassword.value != password.value) {
            confirmPasswordError.value = "Passwords do not match"
            false
        } else {
            confirmPasswordError.value = null
            true
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        if (!validateEmail() or !validatePassword()) return

        loading.value = true
        auth.signInWithEmailAndPassword(email.value, password.value).addOnCompleteListener { task ->
            loading.value = false
            if (task.isSuccessful) {
                if (auth.currentUser?.isEmailVerified == true) {
                    Toast.makeText(context, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Please verify your email first!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        if (!validateEmail() or !validatePassword() or !validateConfirmPassword()) return

        loading.value = true
        auth.createUserWithEmailAndPassword(email.value, password.value).addOnCompleteListener { task ->
            loading.value = false
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.sendEmailVerification()
                val userData = mapOf(
                    "fullName" to fullName.value,
                    "email" to email.value
                )
                db.collection("users").document(user!!.uid).set(userData).addOnSuccessListener {
                    Toast.makeText(context, "Account created! Check your email for verification.", Toast.LENGTH_LONG).show()
                    onSuccess()
                }
            } else {
                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendPasswordReset(onBack: () -> Unit) {
        if (!validateEmail()) return

        auth.sendPasswordResetEmail(email.value).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
