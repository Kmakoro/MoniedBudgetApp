package com.monied.budgetapp.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.monied.budgetapp.R
import com.monied.budgetapp.databinding.ActivityAddExpenseBinding
import com.monied.budgetapp.data.DatabaseHelper
import com.monied.budgetapp.models.Category
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private var _binding: ActivityAddExpenseBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var databaseHelper: DatabaseHelper
    private var categoryList: List<Category> = emptyList()
    private var userId: Int = -1

    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            currentPhotoPath?.let {
                val file = File(it)
                currentPhotoUri = Uri.fromFile(file)
                binding.ivReceiptPhoto.setImageURI(currentPhotoUri)
                binding.ivReceiptPhoto.visibility = View.VISIBLE
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhotoUri = saveImageToInternalStorage(uri)
                binding.ivReceiptPhoto.setImageURI(currentPhotoUri)
                binding.ivReceiptPhoto.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseHelper = DatabaseHelper(requireContext())

        val prefs = requireContext().getSharedPreferences("MoniedPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        setupClickListeners()
        setupSpinner()
        
        // Hide back button if we are in main navigation
        binding.btnBack.visibility = View.GONE
    }

    private fun setupSpinner() {
        categoryList = databaseHelper.getAllCategories(userId)
        val categoryNames = categoryList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etStartTime.setOnClickListener { showTimePicker(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { showTimePicker(binding.etEndTime) }
        binding.btnSaveExpense.setOnClickListener { saveExpense() }
        binding.btnAddPhoto.setOnClickListener { showImagePickerDialog() }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
            val photoFile = try { createImageFile() } catch (ex: IOException) { null }
            photoFile?.also {
                currentPhotoPath = it.absolutePath
                val photoURI = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun removePhoto() {
        currentPhotoUri = null
        binding.ivReceiptPhoto.setImageURI(null)
        binding.ivReceiptPhoto.visibility = View.GONE
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
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

    private fun saveExpense() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val desc = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()

        if (amount == null || desc.isEmpty() || date.isEmpty() || categoryList.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val catId = categoryList[binding.spinnerCategory.selectedItemPosition].id
        val photoUri = currentPhotoUri?.toString()

        val id = databaseHelper.addExpense(amount, desc, date, start, end, catId, userId, photoUri)
        if (id != -1L) {
            Toast.makeText(requireContext(), "Expense added!", Toast.LENGTH_SHORT).show()
            binding.etAmount.text?.clear()
            binding.etDescription.text?.clear()
            removePhoto()
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            binding.etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(target: TextInputEditText) {
        val c = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, m ->
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}