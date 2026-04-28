package com.monied.budgetapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView

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

            val result = dbHelper.registerUser(username, password, fullName, email, phone)
            if (result != -1L) {
                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                getSharedPreferences("MoniedPrefs", MODE_PRIVATE).edit().clear().apply()
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
}