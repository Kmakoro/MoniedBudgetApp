package com.monied.budgetapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var tvRegisterLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                tvError.text = "Please enter username and password"
                tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (dbHelper.checkUserCredentials(username, password)) {
                val user = dbHelper.getUser(username)
                if (user != null) {
                    val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("loggedInUser", username)
                        putInt("userId", user.id)
                        apply()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            } else {
                tvError.text = "Invalid username or password.\nDemo: 'cyril' / 'Password@123'"
                tvError.visibility = android.view.View.VISIBLE
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}