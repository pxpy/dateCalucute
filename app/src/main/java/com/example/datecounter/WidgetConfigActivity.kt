package com.example.datecounter

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class WidgetConfigActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var datePicker: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var modeRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        datePicker = findViewById(R.id.widgetDatePicker)
        confirmButton = findViewById(R.id.confirmButton)
        modeRadioGroup = findViewById(R.id.modeRadioGroup)

        confirmButton.setOnClickListener {
            val selectedDate = LocalDate.of(
                datePicker.year,
                datePicker.month + 1,
                datePicker.dayOfMonth
            )
            
            val isCountDown = modeRadioGroup.checkedRadioButtonId == R.id.countDownRadio

            getSharedPreferences("date_prefs", MODE_PRIVATE).edit().apply {
                putLong("widget_${appWidgetId}_date", selectedDate.toEpochDay())
                putBoolean("widget_${appWidgetId}_countdown", isCountDown)
                apply()
            }

            val appWidgetManager = AppWidgetManager.getInstance(this)
            DateCounterWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
} 