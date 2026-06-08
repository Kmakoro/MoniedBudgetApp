package com.monied.budgetapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters long, and include uppercase, lowercase, numbers, and symbols.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val result = dbHelper.registerUser(username, password, fullName, email, phone)
            if (result != -1L) {
                // Initialize default categories for the new user for isolation
                dbHelper.initializeUserCategories(result.toInt())

                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Username already exists. Please choose another.", Toast.LENGTH_LONG).show()
            }
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Enforces a strong password:
     * - At least 8 characters long
     * - Combines uppercase, lowercase, numbers, and symbols
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")
        return passwordPattern.matcher(password).matches()
    }
}