package com.monied.budgetapp.ui.main

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.models.Category
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etStartTime: TextInputEditText
    private lateinit var etEndTime: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnBack: ImageButton
    private lateinit var btnSaveExpense: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var ivReceiptPhoto: ImageView

    private lateinit var databaseHelper: DatabaseHelper
    private var categoryList: List<Category> = emptyList()
    private var userId: Int = -1

    private var currentPhotoUri: Uri? = null
    private lateinit var currentPhotoPath: String
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY = 2
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        databaseHelper = DatabaseHelper(this)

        // Retrieve userId from SharedPreferences
        val prefs = getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnBack = findViewById(R.id.btnBack)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        ivReceiptPhoto = findViewById(R.id.ivReceiptPhoto)

        setupClickListeners()
        setupSpinner()
        checkPermissions()
    }

    private fun setupSpinner() {
        // Load categories only for the current user to ensure isolation
        categoryList = databaseHelper.getAllCategories(userId)
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Please add a category first in the Categories section.", Toast.LENGTH_LONG).show()
        }
        val categoryNames = categoryList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        etDate.setOnClickListener { showDatePicker() }
        etStartTime.setOnClickListener { showTimePicker(etStartTime) }
        etEndTime.setOnClickListener { showTimePicker(etEndTime) }
        btnSaveExpense.setOnClickListener { saveExpense() }
        btnAddPhoto.setOnClickListener { showImagePickerDialog() }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Receipt Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> openGallery()
                    2 -> removePhoto()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile = try { createImageFile() } catch (ex: IOException) { null }
                photoFile?.also {
                    currentPhotoPath = it.absolutePath
                    val photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun removePhoto() {
        currentPhotoUri = null
        ivReceiptPhoto.setImageURI(null)
        ivReceiptPhoto.visibility = View.GONE
    }

    // In AddExpenseActivity.kt, update onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val file = File(currentPhotoPath)
                    currentPhotoUri = Uri.fromFile(file)
                    ivReceiptPhoto.setImageURI(currentPhotoUri)
                    ivReceiptPhoto.visibility = View.VISIBLE
                }
                REQUEST_GALLERY -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        // Copy to internal storage to persist access
                        currentPhotoUri = saveImageToInternalStorage(selectedImageUri)
                        ivReceiptPhoto.setImageURI(currentPhotoUri)
                        ivReceiptPhoto.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun saveExpense() {
        val amount = etAmount.text.toString().toDoubleOrNull()
        val desc = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()
        val start = etStartTime.text.toString().trim()
        val end = etEndTime.text.toString().trim()

        if (amount == null || desc.isEmpty() || date.isEmpty() || categoryList.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields and ensure a category is selected.", Toast.LENGTH_SHORT).show()
            return
        }

        val catId = categoryList[spinnerCategory.selectedItemPosition].id
        val photoUri = currentPhotoUri?.toString()

        // Fixed: Passing userId as required by DatabaseHelper.addExpense
        val id = databaseHelper.addExpense(amount, desc, date, start, end, catId, userId, photoUri)
        if (id != -1L) {
            Toast.makeText(this, "Expense added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(target: TextInputEditText) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }
}
