package com.example.datecounter

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {
    private lateinit var datePicker: DatePicker
    private lateinit var calculateButton: Button
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        datePicker = findViewById(R.id.datePicker)
        calculateButton = findViewById(R.id.calculateButton)
        resultText = findViewById(R.id.resultText)

        calculateButton.setOnClickListener {
            val selectedDate = LocalDate.of(
                datePicker.year,
                datePicker.month + 1,
                datePicker.dayOfMonth
            )
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(selectedDate, today)
            resultText.text = getString(R.string.days_passed, days)
            
            // 保存选择的日期到 SharedPreferences
            getSharedPreferences("date_prefs", MODE_PRIVATE).edit().apply {
                putLong("saved_date", selectedDate.toEpochDay())
                apply()
            }
        }
    }
} 