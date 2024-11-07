package me.panxin.datecounter

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.RadioGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class WidgetConfigActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var datePicker: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var titleEditText: EditText

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
        titleEditText = findViewById(R.id.titleEditText)

        val prefs = getSharedPreferences("date_prefs", MODE_PRIVATE)
        if (prefs.contains("widget_${appWidgetId}_date")) {
            loadExistingSettings(prefs)
        } else {
            val today = LocalDate.now()
            datePicker.updateDate(today.year, today.monthValue - 1, today.dayOfMonth)
            modeRadioGroup.check(R.id.countUpRadio)
        }

        confirmButton.setOnClickListener {
            val selectedDate = LocalDate.of(
                datePicker.year,
                datePicker.month + 1,
                datePicker.dayOfMonth
            )
            
            val isCountDown = modeRadioGroup.checkedRadioButtonId == R.id.countDownRadio
            val title = titleEditText.text.toString().takeIf { it.isNotBlank() } ?: getString(R.string.default_title)

            getSharedPreferences("date_prefs", MODE_PRIVATE).edit().apply {
                putLong("widget_${appWidgetId}_date", selectedDate.toEpochDay())
                putBoolean("widget_${appWidgetId}_countdown", isCountDown)
                putString("widget_${appWidgetId}_title", title)
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

    private fun loadExistingSettings(prefs: android.content.SharedPreferences) {
        val savedDateEpochDay = prefs.getLong("widget_${appWidgetId}_date", LocalDate.now().toEpochDay())
        val savedDate = LocalDate.ofEpochDay(savedDateEpochDay)
        datePicker.updateDate(
            savedDate.year,
            savedDate.monthValue - 1,
            savedDate.dayOfMonth
        )

        val savedTitle = prefs.getString("widget_${appWidgetId}_title", "")
        if (!savedTitle.isNullOrEmpty()) {
            titleEditText.setText(savedTitle)
        }

        val isCountDown = prefs.getBoolean("widget_${appWidgetId}_countdown", false)
        modeRadioGroup.check(if (isCountDown) R.id.countDownRadio else R.id.countUpRadio)
    }
} 