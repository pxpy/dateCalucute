package me.panxin.datecounter

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class DateCounterWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        // 设置每天午夜更新
        setupMidnightUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        setupMidnightUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // 取消定时更新
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DateCounterWidget::class.java)
        intent.action = ACTION_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                intent.component
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun setupMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DateCounterWidget::class.java)
        intent.action = ACTION_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置下一个午夜时间
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 设置重复闹钟，每24小时触发一次
        alarmManager.setRepeating(
            AlarmManager.RTC,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "me.panxin.datecounter.UPDATE_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("date_prefs", Context.MODE_PRIVATE)
            val savedDateEpochDay = prefs.getLong("widget_${appWidgetId}_date", LocalDate.now().toEpochDay())
            val isCountDown = prefs.getBoolean("widget_${appWidgetId}_countdown", false)
            val title = prefs.getString("widget_${appWidgetId}_title", context.getString(R.string.default_title))
            val savedDate = LocalDate.ofEpochDay(savedDateEpochDay)
            val today = LocalDate.now()
            
            val totalDays = if (isCountDown) {
                ChronoUnit.DAYS.between(today, savedDate)
            } else {
                ChronoUnit.DAYS.between(savedDate, today)
            }
            
            val weeks = kotlin.math.abs(totalDays) / 7
            val remainingDays = kotlin.math.abs(totalDays) % 7

            val views = RemoteViews(context.packageName, R.layout.widget_date_counter)
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_date, context.getString(R.string.date_format, savedDate.toString()))
            
            val daysText = if (isCountDown) {
                if (totalDays > 0) {
                    context.getString(R.string.days_remaining, totalDays)
                } else {
                    context.getString(R.string.days_overdue, kotlin.math.abs(totalDays))
                }
            } else {
                context.getString(R.string.days_passed, totalDays)
            }
            
            views.setTextViewText(R.id.widget_days, daysText)
            views.setTextViewText(R.id.widget_weeks_days, context.getString(R.string.weeks_days_passed, weeks, remainingDays))

            val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
} 