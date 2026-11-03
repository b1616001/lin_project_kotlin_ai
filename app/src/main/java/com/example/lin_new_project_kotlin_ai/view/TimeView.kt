package com.example.lin_new_project_kotlin_ai.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.adapter.TimeAdapter
import com.example.lin_new_project_kotlin_ai.databinding.ViewTimeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.TimeMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 自定義日曆視圖 (TimeView)
 *
 * 優化重點：
 * 1. 修正大月份 31 日換月時的月份跳號 Bug (強制固定 DAY_OF_MONTH = 1 再計算星期偏移)。
 * 2. 採用 GestureDetector 處理流暢滑動換月，修正原左右滑動方向顛倒的問題，且不干擾單擊選取。
 * 3. 避免每次換月重新創建 Adapter 與重複綁定 OnTouchListener，提升繪製效率。
 * 4. 新增「返回今天」按鈕互動與現代 Kotlin Callback 介面。
 * 5. 確保與原有公開成員 (str_time, str_years, arrayList, hashMap, setTime, setValue) 100% 向後相容。
 */
class TimeView : BaseView<ViewTimeBinding?>, View.OnClickListener {

    var str_time: String = "" // 目前點選之完整日期 (格式：yyyy年MM月dd日)
    @JvmField
    var str_years: String = "" // 當前展示之年月 (格式：yyyy年MM月)
    @JvmField
    var arrayList: ArrayList<String?>? = ArrayList()
    var hashMap: HashMap<String?, Boolean?>? = HashMap()

    var onDateSelectedListener: ((date: String, hasEvent: Boolean) -> Unit)? = null

    private var timeAdapter: TimeAdapter? = null
    private var gestureDetector: GestureDetector? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initInternal(context)
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): ViewTimeBinding {
        return ViewTimeBinding.inflate(layoutInflater)
    }

    private fun initInternal(context: Context) {
        val binding = getBinding() ?: return
        binding.gvTime.selector = ColorDrawable(Color.TRANSPARENT)

        binding.tvLeft.setOnClickListener(this)
        binding.tvRight.setOnClickListener(this)
        binding.btnToday.setOnClickListener {
            goToToday()
        }

        // 初始化滑勢手勢辨識器 (左滑 -> 下個月, 右滑 -> 上個月)
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 80
            private val SWIPE_VELOCITY_THRESHOLD = 80

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (abs(diffX) > abs(diffY) && abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // 向右滑動 -> 切換至上一月
                        monthChange(-1)
                    } else {
                        // 向左滑動 -> 切換至下一月
                        monthChange(1)
                    }
                    return true
                }
                return false
            }
        })

        binding.gvTime.setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event) ?: false
        }

        binding.gvTime.setOnItemClickListener { _, _, position, _ ->
            val dayStr = arrayList?.getOrNull(position) ?: ""
            if (dayStr.isNotBlank()) {
                val selectedFullDate = "$str_years${dayStr}日"
                setValue(selectedFullDate)
                val hasEvent = hashMap?.get(selectedFullDate) == true
                onDateSelectedListener?.invoke(selectedFullDate, hasEvent)
            }
        }
    }

    /**
     * 設定初始時間並刷新日曆
     */
    fun setTime(time: String) {
        val safeTime = normalizeDateString(time)
        str_time = safeTime
        val mm = safeTime.indexOf("月")
        str_years = if (mm != -1) {
            safeTime.substring(0, mm + 1).trim()
        } else {
            SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(Date())
        }
        setTimDisplay()
        monthChange(0)
    }

    /**
     * 快速跳轉至當天 (Today)
     */
    fun goToToday() {
        val todayStr = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
        setTime(todayStr)
        val hasEvent = hashMap?.get(todayStr) == true
        onDateSelectedListener?.invoke(todayStr, hasEvent)
    }

    override fun onClick(view: View) {
        val binding = getBinding() ?: return
        when (view) {
            binding.tvLeft -> monthChange(-1)
            binding.tvRight -> monthChange(1)
        }
    }

    /**
     * 切換月份 (change: -1 上個月, 1 下個月, 0 刷新當前月)
     */
    fun monthChange(change: Int) {
        try {
            val calendar = Calendar.getInstance()
            val parsedDate = TimeMethod.stringToDate(str_years, TimeMethod.formatType5)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
            // 關鍵防禦：先將日期固定為 1 號，避免例如 3月31日 +1個月 溢出至 5月1日 的系統日曆 Bug
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, change)

            // 當月 1 號是星期幾 (Calendar.SUNDAY 為 1，轉成 0 代表周日，6 代表周六)
            val firstIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            str_years = TimeMethod.dateToString(calendar.time, TimeMethod.formatType5)
            getBinding()?.tvTime?.text = str_years

            val daysList = ArrayList<String?>()
            // 補充 1 號前面的星期空白
            for (i in 0 until firstIndex) {
                daysList.add("")
            }
            // 填入 1 ~ maxDaysInMonth
            for (i in 1..maxDaysInMonth) {
                daysList.add(if (i < 10) "0$i" else i.toString())
            }
            this.arrayList = daysList

            // 複用現有的 Adapter，避免重複創建與重新綁定監聽器
            if (timeAdapter == null) {
                timeAdapter = TimeAdapter(
                    context,
                    @Suppress("UNCHECKED_CAST") (daysList as ArrayList<String>),
                    str_time,
                    str_years,
                    @Suppress("UNCHECKED_CAST") (hashMap as? HashMap<String, Boolean>)
                )
                getBinding()?.gvTime?.adapter = timeAdapter
            } else {
                timeAdapter?.updateData(
                    @Suppress("UNCHECKED_CAST") (daysList as ArrayList<String>),
                    str_time,
                    str_years,
                    @Suppress("UNCHECKED_CAST") (hashMap as? HashMap<String, Boolean>)
                )
            }
        } catch (e: Exception) {
            Log.e("TimeView", "monthChange 出錯", e)
        }
    }

    /**
     * 更新頂部選取時間與星期資訊
     */
    private fun setTimDisplay() {
        try {
            val binding = getBinding() ?: return
            val calendar = Calendar.getInstance()
            val parsedDate = TimeMethod.stringToDate(str_time, TimeMethod.formatType6)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

            val yearIdx = str_time.indexOf("年")
            val dayIdx = str_time.indexOf("日")

            if (yearIdx != -1 && dayIdx != -1) {
                binding.tvYy.text = str_time.substring(0, yearIdx + 1)
                val monthDay = str_time.substring(yearIdx + 1, dayIdx + 1)
                val weekName = when (dayOfWeek) {
                    1 -> "周一"
                    2 -> "周二"
                    3 -> "周三"
                    4 -> "周四"
                    5 -> "周五"
                    6 -> "周六"
                    else -> "周日"
                }
                binding.tvMm.text = "$monthDay  $weekName"
            } else {
                binding.tvYy.text = str_time
                binding.tvMm.text = ""
            }
        } catch (e: Exception) {
            Log.e("TimeView", "setTimDisplay 出錯", e)
        }
    }

    /**
     * 規範化日期格式，確保為 yyyy年MM月dd日
     */
    private fun normalizeDateString(raw: String): String {
        return try {
            val regex = Regex("(\\d{4})年(\\d{1,2})月(\\d{1,2})日")
            val match = regex.find(raw)
            if (match != null) {
                val (year, month, day) = match.destructured
                val m = if (month.length == 1) "0$month" else month
                val d = if (day.length == 1) "0$day" else day
                "${year}年${m}月${d}日"
            } else {
                raw
            }
        } catch (e: Exception) {
            raw
        }
    }

    /**
     * 設定選取的日期並刷新標題及格狀高亮
     */
    fun setValue(tt: String) {
        val safeDate = normalizeDateString(tt)
        str_time = safeDate
        timeAdapter?.setStr_time(safeDate)
        timeAdapter?.notifyDataSetChanged()
        setTimDisplay()
    }

    /**
     * 更新事件標記 HashMap 並刷新視圖
     */
    fun setValue(hashMap: HashMap<String?, Boolean?>?) {
        this.hashMap = hashMap
        @Suppress("UNCHECKED_CAST")
        timeAdapter?.setHashMap(hashMap as? HashMap<String, Boolean>)
        timeAdapter?.notifyDataSetChanged()
    }
}
