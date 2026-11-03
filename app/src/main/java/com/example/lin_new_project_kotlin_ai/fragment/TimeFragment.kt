package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTimeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日曆展示頁面 (TimeFragment)
 *
 * 優化重點：
 * 1. 動態初始化：預設載入真實系統當天日期，不再死板綁定固定歷史日期。
 * 2. 行程動態互動：支援即時標記/取消行程，並雙向同步日曆格子高亮與下方行程資訊卡片。
 * 3. 雙向回呼聯動：接取 TimeView 點選與切換事件，即時更新當日行程詳細狀態與統計。
 * 4. 異常與空值防護：全面淘汰非空強制斷言 (!!)，加強邊界處理。
 */
class TimeFragment : BaseBindingFragment<FragmentTimeBinding?>() {

    private val eventMap = HashMap<String?, Boolean?>()
    private var currentSelectedDate: String = ""

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTimeBinding {
        return FragmentTimeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        // 1. 初始化預設行程資料 (以當前年份與月份動態生成幾個行程示例)
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val monthStr = if (currentMonth < 10) "0$currentMonth" else "$currentMonth"
        val prefix = "${currentYear}年${monthStr}月"

        eventMap["${prefix}01日"] = true
        eventMap["${prefix}06日"] = true
        eventMap["${prefix}08日"] = true
        eventMap["${prefix}12日"] = true
        eventMap["${prefix}17日"] = true
        eventMap["${prefix}25日"] = true

        // 2. 取得今日日期
        val todayStr = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
        currentSelectedDate = todayStr

        // 3. 綁定至 TimeView
        binding.timeView.hashMap = eventMap
        binding.timeView.setTime(todayStr)

        // 4. 更新下方詳情資訊卡片
        updateDetailCard(todayStr)

        // 5. 設定日曆點選回呼監聽
        binding.timeView.onDateSelectedListener = { selectedDate, hasEvent ->
            currentSelectedDate = selectedDate
            updateDetailCard(selectedDate)
            ToastMethod.showToast(requireContext(), selectedDate)
        }

        // 6. 行程切換操作按鈕 (即時新增/取消當日標記)
        binding.btnToggleEvent.setOnClickListener {
            if (currentSelectedDate.isBlank()) return@setOnClickListener

            val currentHasEvent = eventMap[currentSelectedDate] == true
            if (currentHasEvent) {
                eventMap.remove(currentSelectedDate)
                ToastMethod.showToast(requireContext(), "已取消 $currentSelectedDate 的標記")
            } else {
                eventMap[currentSelectedDate] = true
                ToastMethod.showToast(requireContext(), "已將 $currentSelectedDate 加入標記行程")
            }

            // 同步刷新日曆格子與資訊面板
            binding.timeView.setValue(eventMap)
            updateDetailCard(currentSelectedDate)
        }
    }

    /**
     * 更新下方選取日期的詳細狀態卡片
     */
    private fun updateDetailCard(dateStr: String) {
        val binding = getBinding() ?: return
        binding.tvDetailDate.text = dateStr

        val hasEvent = eventMap[dateStr] == true
        if (hasEvent) {
            binding.tvEventStatusBadge.text = "● 今日有標記行程"
            binding.tvEventStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
            binding.tvEventStatusBadge.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_light)
            binding.btnToggleEvent.text = "取消此日行程標記"
            binding.btnToggleEvent.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark)
        } else {
            binding.tvEventStatusBadge.text = "○ 一般日期 (無特殊排程)"
            binding.tvEventStatusBadge.setTextColor(Color.parseColor("#757575"))
            binding.tvEventStatusBadge.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
            binding.btnToggleEvent.text = "標記此日為行程日"
            binding.btnToggleEvent.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), android.R.color.holo_blue_dark)
        }

        // 更新總標記天數
        val totalCount = eventMap.filterValues { it == true }.size
        binding.tvTotalEventsBadge.text = "本月已標記 ${totalCount} 天"
    }
}