package com.monied.budgetapp.dialog


import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.monied.budgetapp.R
import java.util.*

class DateRangePickerDialog : DialogFragment() {

    private lateinit var onRangeSelectedListener: (startDate: String, endDate: String) -> Unit
    private var startDate: String = ""
    private var endDate: String = ""

    companion object {
        fun newInstance(
            currentStartDate: String? = null,
            currentEndDate: String? = null,
            listener: (startDate: String, endDate: String) -> Unit
        ): DateRangePickerDialog {
            val dialog = DateRangePickerDialog()
            dialog.onRangeSelectedListener = listener
            dialog.startDate = currentStartDate ?: getDefaultStartDate()
            dialog.endDate = currentEndDate ?: getCurrentDate()
            return dialog
        }

        private fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            return String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        private fun getDefaultStartDate(): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1) // Last 30 days
            return String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_date_range_picker, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupPeriods)
        val btnStartDate = view.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view.findViewById<Button>(R.id.btnEndDate)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDateValue)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDateValue)

        updateDateDisplay(tvStartDate, startDate)
        updateDateDisplay(tvEndDate, endDate)

        // Preset period listeners
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioToday -> {
                    val today = getCurrentDate()
                    startDate = today
                    endDate = today
                    updateDateDisplay(tvStartDate, startDate)
                    updateDateDisplay(tvEndDate, endDate)
                }
                /*R.id.radioYesterday -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterday = formatDate(calendar)
                    startDate = yesterday
                    endDate = yesterday
                    updateDateDisplay(tvStartDate, startDate)
                    updateDateDisplay(tvEndDate, endDate)
                }*/
                R.id.radioThisWeek -> {
                    val calendar = Calendar.getInstance()
                    calendar.firstDayOfWeek = Calendar.MONDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    startDate = formatDate(calendar)
                    endDate = getCurrentDate()
                    updateDateDisplay(tvStartDate, startDate)
                    updateDateDisplay(tvEndDate, endDate)
                }
                R.id.radioThisMonth -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    startDate = formatDate(calendar)
                    endDate = getCurrentDate()
                    updateDateDisplay(tvStartDate, startDate)
                    updateDateDisplay(tvEndDate, endDate)
                }
                /* R.id.radioLastMonth -> {
                     val calendar = Calendar.getInstance()
                     calendar.add(Calendar.MONTH, -1)
                     calendar.set(Calendar.DAY_OF_MONTH, 1)
                     startDate = formatDate(calendar)
                     calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                     endDate = formatDate(calendar)
                     updateDateDisplay(tvStartDate, startDate)
                     updateDateDisplay(tvEndDate, endDate)
                 }*/
                R.id.radioLast30Days -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                    startDate = formatDate(calendar)
                    endDate = getCurrentDate()
                    updateDateDisplay(tvStartDate, startDate)
                    updateDateDisplay(tvEndDate, endDate)
                }
            }
        }

        btnStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                updateDateDisplay(tvStartDate, startDate)
                radioGroup.clearCheck()
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                updateDateDisplay(tvEndDate, endDate)
                radioGroup.clearCheck()
            }
        }

        builder.setTitle("Select Date Range")
            .setView(view)
            .setPositiveButton("View Expenses") { _, _ ->
                onRangeSelectedListener(startDate, endDate)
            }
            .setNegativeButton("Cancel", null)

        return builder.create()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(date)
        }, year, month, day).show()
    }

    private fun updateDateDisplay(textView: TextView, date: String) {
        try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            textView.text = outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            textView.text = date
        }
    }

    private fun formatDate(calendar: Calendar): String {
        return String.format("%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}