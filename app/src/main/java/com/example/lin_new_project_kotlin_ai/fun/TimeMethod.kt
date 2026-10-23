package com.example.lin_new_project_kotlin_ai.`fun`

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import com.example.lin_new_project_kotlin_ai.R
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeMethod {
    const val formatType: String = "yyyy-MM-dd HH:mm:ss"
    const val formatType2: String = "yyyy年-MM月"
    const val formatType3: String = "yyyy-MM-dd"
    const val formatType4: String = "MM-dd"
    const val formatType5: String = "yyyy年MM月"
    const val formatType6: String = "yyyy年MM月dd日"

    const val zone_UTC: String = "UTC"
    const val zone_GMT_8: String = "UTC+8"
    const val zone_GMT_9: String = "UTC+9"

    fun getTime_zone(formatType: String?, zone: String?): String {
        //取得目前年月日時間
        val formatter = SimpleDateFormat(formatType, Locale.getDefault())
        val timeZone = TimeZone.getTimeZone(zone)
        formatter.setTimeZone(timeZone)
        val curDate = Date(System.currentTimeMillis()) // 獲取當前時間
        return formatter.format(curDate)
    }

    fun getTime(formatType: String?): String {
        //取得目前年月日時間
        val formatter = SimpleDateFormat(formatType, Locale.getDefault())
        val curDate = Date(System.currentTimeMillis()) // 獲取當前時間
        return formatter.format(curDate)
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // com.example.lin_new_project_kotlin.data Date类型的时间
    fun dateToString(data: Date?, formatType: String?): String {
        //date類型轉換為String類型
        return SimpleDateFormat(formatType, Locale.getDefault()).format(data)
    }


    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    @Throws(ParseException::class)
    fun longToString(currentTime: Long, formatType: String?): String {
        //long類型轉換為String類型
        val date = longToDate(currentTime, formatType) // long类型转成Date类型
        return dateToString(date, formatType) // date类型转成String
    }

    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    @Throws(ParseException::class)
    fun stringToDate(strTime: String, formatType: String?): Date? {
        //string類型轉換為date類型
        val formatter = SimpleDateFormat(formatType, Locale.getDefault())
        return formatter.parse(strTime)
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    @Throws(ParseException::class)
    fun longToDate(currentTime: Long, formatType: String?): Date {
        //long轉換為Date類型
        val dateOld = Date(currentTime) // 根据long类型的毫秒数生命一个date类型的时间
        val sDateTime = dateToString(dateOld, formatType) // 把date类型的时间转换为string
        return stringToDate(sDateTime, formatType)!! // 把String类型转换为Date类型
    }

    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    @Throws(ParseException::class)
    fun stringToLong(strTime: String, formatType: String?): Long {
        //String類型轉換為long類型
        val date = stringToDate(strTime, formatType) // String类型转成date类型
        if (date == null) {
            return 0
        } else {
            return dateToLong(date) // date类型转成long类型
        }
    }

    // date要转换的date类型的时间
    fun dateToLong(date: Date): Long {
        //date類型轉換為long類型
        return date.getTime()
    }

    @Throws(ParseException::class)
    fun addDayMINUTE(strTime: String, days: Int, formatType: String?): String {
        val date = stringToDate(strTime, formatType)
        val calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + days)
        return dateToString(calendar.getTime(), formatType)
    }

    fun reduceDayMINUTE(strTime: String, days: Int, formatType: String?): String {
        try {
            val date = stringToDate(strTime, formatType)
            val calendar = Calendar.getInstance()
            calendar.setTime(date)

            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - days)
            return dateToString(calendar.getTime(), formatType)
        } catch (e: Exception) {
            return strTime
        }
    }

    private fun str3Date(str: String, formatType: String?): Date? {
        try {
            val df = SimpleDateFormat(formatType)
            return df.parse(str)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun setTimDisplay(resources: Resources, time: String, formatType: String?): String {
        val calendar = Calendar.getInstance()
        calendar.setTime(str3Date(time, formatType))
        val ii = time.indexOf("年")

        var MM = time.substring(ii + 1)
        MM = MM.replace("月", "-")
        MM = MM.replace("日", "")
        val firstIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        var qq = ""
        when (firstIndex) {
            1 -> qq = resources.getString(R.string.monday)
            2 -> qq = resources.getString(R.string.tuesday)
            3 -> qq = resources.getString(R.string.wednesday)
            4 -> qq = resources.getString(R.string.thursday)
            5 -> qq = resources.getString(R.string.friday)
            6 -> qq = resources.getString(R.string.saturday)
            0 -> qq = resources.getString(R.string.sunday)
        }
        return MM + "\n" + qq
    }

    fun datePicker(
        context: Context, time: String, formatType: String?,
        onDateSetListener: OnDateSetListener
    ) {
        var time = time
        if (time.trim { it <= ' ' }.length == 0) {
            time = getTime(formatType)
        }
        val calendar = Calendar.getInstance()
        try {
            calendar.setTime(stringToDate(time, formatType))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)
        val dd = calendar.get(Calendar.DAY_OF_MONTH)
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    val curDate = calendar.getTime()
                    val time = dateToString(curDate, formatType)
                    onDateSetListener.onDateSet(time)
                }
            }

        DatePickerDialog(context, dateSetListener, yy, mm, dd).show()
    }

    fun datePicker_years(
        context: Context?, time: String, formatType: String?,
        onDateSetListener: OnDateSetListener
    ) {
        var time = time
        if (time.trim { it <= ' ' }.length == 0) {
            time = getTime(formatType)
        }
        val calendar = Calendar.getInstance()
        try {
            calendar.setTime(stringToDate(time, formatType))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)

        val dateSetListener: DatePickerDialog.OnDateSetListener =
            object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
                    val decimalFormat = DecimalFormat("00") //十進制

                    onDateSetListener.onDateSet(
                        decimalFormat.format(year.toLong()) + "-" + decimalFormat.format(
                            month.toLong()
                        )
                    )
                }
            }
        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(
                context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar
            ), dateSetListener, yy, mm, 12
        )
        ((datePickerDialog.getDatePicker()
            .getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(2).setVisibility(
            View.GONE
        )
        datePickerDialog.show()
    }

    fun datePicker_TimePickerDialog(
        context: Context?, time: String, formatType: String?,
        onTimeSetListener: onTimeSetListener
    ) {
        var time = time
        if (time.trim { it <= ' ' }.length == 0) {
            time = getTime(formatType)
        }
        val calendar = Calendar.getInstance()

        try {
            calendar.setTime(stringToDate(time, formatType))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val hh = calendar.get(Calendar.HOUR_OF_DAY)
        val mm = calendar.get(Calendar.MINUTE)
        val timePickerDialog: TimePickerDialog =
            TimePickerDialog(context, R.style.abirStyle, object :
                TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    val decimalFormat = DecimalFormat("00") //十進制
                    onTimeSetListener.onTimeSet(
                        decimalFormat.format(hourOfDay.toLong()) + ":" + decimalFormat.format(
                            minute.toLong()
                        )
                    )
                }
            }, hh, mm, true)
        //        timePickerDialog.setTitle("pick");
        timePickerDialog.show()
    }

    interface OnDateSetListener {
        /**
         * 介面中的點選每一項的實現方法，引數自己定義
         * @param time 點選的時間
         */
        fun onDateSet(time: String?)
    }

    interface onTimeSetListener {
        /**
         * 介面中的點選每一項的實現方法，引數自己定義
         * @param time 點選的時間
         */
        fun onTimeSet(time: String?)
    }
}
