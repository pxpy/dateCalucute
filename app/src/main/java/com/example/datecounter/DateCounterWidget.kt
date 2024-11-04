package com.example.datecounter

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DateCounterWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("date_prefs", Context.MODE_PRIVATE)
        val savedDateEpochDay = prefs.getLong("saved_date", LocalDate.now().toEpochDay())
        val savedDate = LocalDate.ofEpochDay(savedDateEpochDay)
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(savedDate, today)

        val views = RemoteViews(context.packageName, R.layout.widget_date_counter)
        views.setTextViewText(R.id.widget_date, context.getString(R.string.date_format, savedDate.toString()))
        views.setTextViewText(R.id.widget_days, context.getString(R.string.days_passed, days))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
} 