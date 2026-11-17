package me.panxin.datecounter

import android.content.Context
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * 把两个日期之间的间隔格式化为「周 + 天」或「月 + 天」。
 *
 * 间隔不超过 50 周时按周展示，例如「3周2天」；
 * 超过 50 周后按月展示，例如「13个月4天」。
 */
object DateSpanFormatter {

    /** 50 周对应的天数，超过后改用月为单位展示 */
    private const val MONTH_UNIT_THRESHOLD_DAYS = 50L * 7

    fun format(context: Context, start: LocalDate, end: LocalDate): String {
        val totalDays = abs(ChronoUnit.DAYS.between(start, end))

        if (totalDays <= MONTH_UNIT_THRESHOLD_DAYS) {
            return context.getString(
                R.string.weeks_days_passed,
                (totalDays / 7).toInt(),
                (totalDays % 7).toInt()
            )
        }

        val earlier = if (start.isAfter(end)) end else start
        val later = if (start.isAfter(end)) start else end
        // 按自然月拆解，兼容每月天数不同的情况
        val period = Period.between(earlier, later)
        val months = period.years * 12 + period.months

        return if (period.days > 0) {
            context.getString(R.string.months_days_passed, months, period.days)
        } else {
            context.getString(R.string.months_passed, months)
        }
    }
}
