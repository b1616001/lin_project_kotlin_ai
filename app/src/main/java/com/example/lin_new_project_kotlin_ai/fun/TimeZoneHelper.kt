package com.example.lin_new_project_kotlin_ai.`fun`

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

data class TimeZonePreset(
    val zoneId: String,
    val displayName: String,
    val regionLabel: String
)

data class ConvertedTimeResult(
    val zoneId: String,
    val displayName: String,
    val formattedDateTime: String,
    val formattedDate: String,
    val formattedTime: String,
    val weekdayText: String,
    val offsetText: String,
    val iso8601: String,
    val isDaylightSaving: Boolean,
    val dayDelta: Int,
    val dayDeltaText: String
)

object TimeZoneHelper {

    val popularZones: List<TimeZonePreset> = listOf(
        TimeZonePreset("UTC", "世界協調時間 (UTC)", "標準時間"),
        TimeZonePreset("Asia/Taipei", "台北 Taipei", "亞洲"),
        TimeZonePreset("Asia/Tokyo", "東京 Tokyo", "亞洲"),
        TimeZonePreset("Asia/Seoul", "首爾 Seoul", "亞洲"),
        TimeZonePreset("Asia/Shanghai", "上海 Shanghai", "亞洲"),
        TimeZonePreset("Asia/Hong_Kong", "香港 Hong Kong", "亞洲"),
        TimeZonePreset("Asia/Singapore", "新加坡 Singapore", "亞洲"),
        TimeZonePreset("Asia/Bangkok", "曼谷 Bangkok", "亞洲"),
        TimeZonePreset("Asia/Dubai", "杜拜 Dubai", "中東"),
        TimeZonePreset("Europe/London", "倫敦 London", "歐洲"),
        TimeZonePreset("Europe/Paris", "巴黎 Paris", "歐洲"),
        TimeZonePreset("Europe/Berlin", "柏林 Berlin", "歐洲"),
        TimeZonePreset("America/New_York", "紐約 New York", "美洲"),
        TimeZonePreset("America/Chicago", "芝加哥 Chicago", "美洲"),
        TimeZonePreset("America/Los_Angeles", "洛杉磯 Los Angeles", "美洲"),
        TimeZonePreset("Australia/Sydney", "雪梨 Sydney", "大洋洲"),
        TimeZonePreset("Pacific/Auckland", "奧克蘭 Auckland", "大洋洲")
    )

    /**
     * Resolves both IANA IDs (e.g. "Asia/Taipei"), "UTC", "GMT+8", and "UTC+8" / "UTC-05:30"
     * cleanly in java.util.TimeZone (since TimeZone.getTimeZone("UTC+8") normally falls back to GMT+0).
     */
    fun resolveTimeZone(zone: String?): TimeZone {
        val raw = zone?.trim().orEmpty()
        if (raw.isEmpty()) return TimeZone.getDefault()
        if (raw.equals("UTC", ignoreCase = true) || raw.equals("Z", ignoreCase = true)) {
            return TimeZone.getTimeZone("UTC")
        }
        if (raw.startsWith("UTC+", ignoreCase = true) || raw.startsWith("UTC-", ignoreCase = true)) {
            val normalizedGmt = "GMT" + raw.substring(3)
            return TimeZone.getTimeZone(normalizedGmt)
        }
        return TimeZone.getTimeZone(raw)
    }

    fun findDisplayName(zoneId: String): String {
        popularZones.firstOrNull { it.zoneId == zoneId }?.let { return it.displayName }
        return zoneId.substringAfterLast('/').replace('_', ' ')
    }

    fun formatOffsetString(timeZone: TimeZone, atMillis: Long = System.currentTimeMillis()): String {
        val offsetMillis = timeZone.getOffset(atMillis)
        val totalMinutes = offsetMillis / 60_000
        val sign = if (totalMinutes >= 0) "+" else "-"
        val absMinutes = abs(totalMinutes)
        val hours = absMinutes / 60
        val mins = absMinutes % 60
        return String.format(Locale.US, "UTC%s%02d:%02d", sign, hours, mins)
    }

    fun formatIsoOffsetSuffix(timeZone: TimeZone, atMillis: Long): String {
        val offsetMillis = timeZone.getOffset(atMillis)
        if (offsetMillis == 0) return "Z"
        val totalMinutes = offsetMillis / 60_000
        val sign = if (totalMinutes >= 0) "+" else "-"
        val absMinutes = abs(totalMinutes)
        val hours = absMinutes / 60
        val mins = absMinutes % 60
        return String.format(Locale.US, "%s%02d:%02d", sign, hours, mins)
    }

    fun convertTimestamp(
        epochMillis: Long,
        targetZoneId: String,
        referenceZoneId: String = TimeZone.getDefault().id
    ): ConvertedTimeResult {
        val targetTz = resolveTimeZone(targetZoneId)
        val refTz = resolveTimeZone(referenceZoneId)
        val date = Date(epochMillis)

        val dateTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = targetTz
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = targetTz
        }
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            timeZone = targetTz
        }
        val weekdayFmt = SimpleDateFormat("EEE", Locale.TRADITIONAL_CHINESE).apply {
            timeZone = targetTz
        }
        val isoBaseFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = targetTz
        }

        val dayDelta = calculateDayDelta(epochMillis, refTz, targetTz)
        val dayDeltaText = when {
            dayDelta > 0 -> "+$dayDelta 天"
            dayDelta < 0 -> "$dayDelta 天"
            else -> "同一天"
        }

        return ConvertedTimeResult(
            zoneId = targetZoneId,
            displayName = findDisplayName(targetZoneId),
            formattedDateTime = dateTimeFmt.format(date),
            formattedDate = dateFmt.format(date),
            formattedTime = timeFmt.format(date),
            weekdayText = weekdayFmt.format(date),
            offsetText = formatOffsetString(targetTz, epochMillis),
            iso8601 = isoBaseFmt.format(date) + formatIsoOffsetSuffix(targetTz, epochMillis),
            isDaylightSaving = targetTz.inDaylightTime(date),
            dayDelta = dayDelta,
            dayDeltaText = dayDeltaText
        )
    }

    fun calculateDayDelta(epochMillis: Long, sourceTz: TimeZone, targetTz: TimeZone): Int {
        val srcDay = dayNumberInZone(epochMillis, sourceTz)
        val dstDay = dayNumberInZone(epochMillis, targetTz)
        return (dstDay - srcDay).toInt()
    }

    private fun dayNumberInZone(epochMillis: Long, tz: TimeZone): Long {
        val cal = Calendar.getInstance(tz).apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Convert local midnight to UTC day index safely using calendar year/day
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
        return utcCal.timeInMillis / 86_400_000L
    }

    fun calculateOffsetDiffText(
        sourceZoneId: String,
        targetZoneId: String,
        atMillis: Long = System.currentTimeMillis()
    ): String {
        val srcOffset = resolveTimeZone(sourceZoneId).getOffset(atMillis)
        val dstOffset = resolveTimeZone(targetZoneId).getOffset(atMillis)
        val diffMinutes = (dstOffset - srcOffset) / 60_000
        if (diffMinutes == 0) return "無時差（同步）"

        val direction = if (diffMinutes > 0) "快" else "慢"
        val absMin = abs(diffMinutes)
        val hours = absMin / 60
        val mins = absMin % 60
        return when {
            hours > 0 && mins > 0 -> "比來源$direction $hours 小時 $mins 分鐘"
            hours > 0 -> "比來源$direction $hours 小時"
            else -> "比來源$direction $mins 分鐘"
        }
    }

    fun buildInstantFromZoneFields(
        year: Int,
        monthZeroBased: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int,
        second: Int,
        sourceZoneId: String
    ): Long {
        val tz = resolveTimeZone(sourceZoneId)
        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(year, monthZeroBased, dayOfMonth, hourOfDay, minute, second)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun extractZoneFields(epochMillis: Long, zoneId: String): Calendar {
        return Calendar.getInstance(resolveTimeZone(zoneId)).apply {
            timeInMillis = epochMillis
        }
    }

    /**
     * Parses flexible user input:
     * - 10-digit Unix seconds (e.g. 1795000000)
     * - 13-digit Unix milliseconds (e.g. 1795000000000)
     * - DateTime strings ("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd") in sourceZoneId
     */
    fun parseFlexibleInput(input: String, sourceZoneId: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val numeric = trimmed.toLongOrNull()
        if (numeric != null) {
            return if (abs(numeric) < 100_000_000_000L) {
                numeric * 1000L
            } else {
                numeric
            }
        }

        val tz = resolveTimeZone(sourceZoneId)
        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = tz
                }
                val parsed = fmt.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return null
    }

    fun getAllSelectableZones(): List<TimeZonePreset> {
        val popularIds = popularZones.map { it.zoneId }.toSet()
        val extra = TimeZone.getAvailableIDs()
            .filter { id ->
                id !in popularIds &&
                    id.contains("/") &&
                    !id.startsWith("SystemV/") &&
                    !id.startsWith("Etc/")
            }
            .sorted()
            .map { id ->
                val region = id.substringBefore('/')
                val city = id.substringAfterLast('/').replace('_', ' ')
                TimeZonePreset(id, city, region)
            }
        return popularZones + extra
    }
}
