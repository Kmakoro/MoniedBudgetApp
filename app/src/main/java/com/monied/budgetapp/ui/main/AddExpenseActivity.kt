package com.monied.budgetapp.ui.main

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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

    // UI Components
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

    // Database and Data
    private lateinit var databaseHelper: DatabaseHelper
    private var categoryList: List<Category> = emptyList()

    // Photo variables
    private var currentPhotoUri: Uri? = null
    private lateinit var currentPhotoPath: String
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY = 2
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        databaseHelper = DatabaseHelper(this)

        // Find views
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
        categoryList = databaseHelper.getAllCategories()
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Please create a category first!", Toast.LENGTH_LONG).show()
            return
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
                val photoFile = createImageFile()
                currentPhotoPath = photoFile.absolutePath
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "temp_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun removePhoto() {
        currentPhotoUri = null
        ivReceiptPhoto.setImageURI(null)
        ivReceiptPhoto.visibility = android.view.View.GONE
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val tempFile = File(currentPhotoPath)
                    if (tempFile.exists()) {
                        // Copy to permanent location
                        val permanentFile = copyToPermanentStorage(tempFile)
                        if (permanentFile != null) {
                            currentPhotoUri = Uri.fromFile(permanentFile)
                            ivReceiptPhoto.setImageURI(currentPhotoUri)
                            ivReceiptPhoto.visibility = android.view.View.VISIBLE
                        } else {
                            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        // Copy gallery image to permanent storage
                        val permanentFile = copyGalleryImageToPermanent(uri)
                        if (permanentFile != null) {
                            currentPhotoUri = Uri.fromFile(permanentFile)
                            ivReceiptPhoto.setImageURI(currentPhotoUri)
                            ivReceiptPhoto.visibility = android.view.View.VISIBLE
                        } else {
                            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun copyToPermanentStorage(sourceFile: File): File? {
        return try {
            val destDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts")
            if (!destDir.exists()) destDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destFile = File(destDir, "receipt_${timeStamp}.jpg")
            val inputStream = sourceFile.inputStream()
            val outputStream = FileOutputStream(destFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            sourceFile.delete() // delete temp file
            destFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun copyGalleryImageToPermanent(sourceUri: Uri): File? {
        return try {
            val destDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "receipts")
            if (!destDir.exists()) destDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destFile = File(destDir, "receipt_${timeStamp}.jpg")
            val inputStream = contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val denied = grantResults.any { it != PackageManager.PERMISSION_GRANTED }
            if (denied) {
                Toast.makeText(this, "Camera/Storage permission needed to add photos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveExpense() {
        val amountString = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()

        if (amountString.isEmpty() || description.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "No category selected", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = spinnerCategory.selectedItemPosition
        val selectedCategoryId = categoryList[selectedIndex].id

        val photoUriString = currentPhotoUri?.toString()

        val result = databaseHelper.addExpense(
            amount = amount,
            description = description,
            date = date,
            startTime = startTime,
            endTime = endTime,
            categoryId = selectedCategoryId,
            photoUri = photoUriString
        )

        if (result != -1L) {
            Toast.makeText(this, "Expense Saved Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
            etDate.setText(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun showTimePicker(targetEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hour, minute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            targetEditText.setText(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }
}