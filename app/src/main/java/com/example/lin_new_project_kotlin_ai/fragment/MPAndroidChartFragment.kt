package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentMpandroidChartBinding
import com.example.lin_new_project_kotlin_ai.`fun`.PieChartMethod
import com.example.lin_new_project_kotlin_ai.view.Radar2View
import com.example.lin_new_project_kotlin_ai.view.RadarView
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlin.random.Random

/**
 * 圖表展示頁面 (MPAndroidChartFragment)
 *
 * 優化重點：
 * 1. 預設顯示修正：進入畫面即預設選中並展示「圓環圖」，徹底解決原先空白無內容的問題。
 * 2. 現代 Segmented Tab 標籤切換：切換圓環圖、橫長條圖與雷達圖時同步高亮當前標籤樣式。
 * 3. 實用資料集與互動：
 *    - 圓環圖：生活化分類佔比數據、支援切換「實心/甜甜圈環」孔徑、支援隨機換數與扇區點選詳情顯示。
 *    - 橫向堆疊長條圖：精簡為 6 項關鍵業務指標達成率，修正原 5000px 硬編碼導致被裁切且無法捲動的嚴重問題。
 *    - 雷達圖：啟用五維與六維完整多邊形/頂點/文字/半透明覆蓋繪製，並提供一鍵隨機能力值刷新。
 * 4. 程式碼健全度：全面消除非空斷言 (!!)，加強邊界檢查。
 */
class MPAndroidChartFragment : BaseBindingFragment<FragmentMpandroidChartBinding?>() {

    private var radar2View: Radar2View? = null
    private var radarView: RadarView? = null

    private var isPieHoleEnabled = true
    private var currentTab = TAB_PIE

    private val pieCategories = arrayOf("餐飲美食", "居家生活", "交通出行", "休閒娛樂", "生活購物", "醫療保健")
    private val pieValues = ArrayList<Int?>()

    private val barMetrics = arrayOf("客戶滿意度", "銷售達成率", "系統穩定度", "轉換留存率", "回購比率", "行銷推廣")

    companion object {
        private const val TAB_PIE = 0
        private const val TAB_BAR = 1
        private const val TAB_RADAR = 2
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentMpandroidChartBinding {
        return FragmentMpandroidChartBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        // 綁定頁籤點選事件
        binding.btnChart.setOnClickListener { switchTab(TAB_PIE) }
        binding.btnStackedBar.setOnClickListener { switchTab(TAB_BAR) }
        binding.btnRadarChart.setOnClickListener { switchTab(TAB_RADAR) }

        // 初始化圓環圖
        initPieChart()

        // 圓環圖互動按鈕
        binding.btnTogglePieHole.setOnClickListener {
            isPieHoleEnabled = !isPieHoleEnabled
            PieChartMethod.ini(binding.PieChartC1, isPieHoleEnabled)
            binding.PieChartC1.invalidate()
        }

        binding.btnRandomPieData.setOnClickListener {
            generateRandomPieData()
        }

        // 長條圖互動按鈕
        binding.btnRandomBarData.setOnClickListener {
            generateRandomBarData()
        }

        // 雷達圖互動按鈕
        binding.btnRandomRadar.setOnClickListener {
            randomizeRadarData()
        }

        // 預設切換至第一頁 (圓環圖)
        switchTab(TAB_PIE)
    }

    /**
     * 切換頁籤並同步高亮按鈕樣式與可見度
     */
    private fun switchTab(tab: Int) {
        val binding = getBinding() ?: return
        currentTab = tab

        val activeBg = Color.parseColor("#1976D2")
        val inactiveBg = Color.parseColor("#E0E0E0")
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.parseColor("#424242")

        // 重置所有按鈕外觀
        binding.btnChart.setBackgroundColor(if (tab == TAB_PIE) activeBg else inactiveBg)
        binding.btnChart.setTextColor(if (tab == TAB_PIE) activeTextColor else inactiveTextColor)

        binding.btnStackedBar.setBackgroundColor(if (tab == TAB_BAR) activeBg else inactiveBg)
        binding.btnStackedBar.setTextColor(if (tab == TAB_BAR) activeTextColor else inactiveTextColor)

        binding.btnRadarChart.setBackgroundColor(if (tab == TAB_RADAR) activeBg else inactiveBg)
        binding.btnRadarChart.setTextColor(if (tab == TAB_RADAR) activeTextColor else inactiveTextColor)

        // 切換各圖表容器之可見狀態
        binding.linearLayoutChart.visibility = if (tab == TAB_PIE) View.VISIBLE else View.GONE
        binding.linearLayoutStackedBar.visibility = if (tab == TAB_BAR) View.VISIBLE else View.GONE
        binding.linearLayoutRadarChart.visibility = if (tab == TAB_RADAR) View.VISIBLE else View.GONE

        // 延遲初始化特定頁面
        if (tab == TAB_BAR && binding.horizontalBarChart.data == null) {
            initStackedBar()
        } else if (tab == TAB_RADAR) {
            initRadarViews()
        }
    }

    /**
     * 初始化圓環圖
     */
    private fun initPieChart() {
        val binding = getBinding() ?: return
        PieChartMethod.ini(binding.PieChartC1, isPieHoleEnabled)

        // 監聽點選特定扇區
        binding.PieChartC1.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val index = h?.x?.toInt() ?: return
                if (index in pieCategories.indices && index in pieValues.indices) {
                    val category = pieCategories[index]
                    val value = pieValues[index] ?: 0
                    val total = pieValues.filterNotNull().sum().coerceAtLeast(1)
                    val percent = String.format("%.1f", (value.toFloat() / total * 100))
                    binding.tvPieSelectedItem.text = "類別：$category\n數值：$value ($percent %)"
                }
            }

            override fun onNothingSelected() {
                binding.tvPieSelectedItem.text = "請點選上方圖表查看明細"
            }
        })

        generateRandomPieData()
    }

    /**
     * 隨機生成圓環圖數據
     */
    private fun generateRandomPieData() {
        val binding = getBinding() ?: return
        val categoryList = ArrayList<String?>()
        pieValues.clear()

        for (category in pieCategories) {
            categoryList.add(category)
            pieValues.add(Random.nextInt(15, 80))
        }

        PieChartMethod.setData(requireContext(), binding.PieChartC1, categoryList, pieValues)
        binding.tvPieSelectedItem.text = "數據已刷新，點選扇區可查看細節"
    }

    /**
     * 初始化橫向堆疊長條圖 (Horizontal Stacked Bar Chart)
     */
    private fun initStackedBar() {
        val binding = getBinding() ?: return
        val chart = binding.horizontalBarChart

        chart.description.isEnabled = false
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(false)
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setFitBars(true)

        // X 軸 (指標項目)
        val xl: XAxis = chart.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.granularity = 1f
        xl.labelCount = barMetrics.size
        xl.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx in barMetrics.indices) barMetrics[idx] else ""
            }
        }

        // Y 軸 (數值)
        chart.axisRight.isEnabled = false
        val yl: YAxis = chart.axisLeft
        yl.setDrawAxisLine(true)
        yl.setDrawGridLines(true)
        yl.axisMinimum = 0f
        yl.axisMaximum = 100f
        yl.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
        }

        // 圖例 Legend
        val legend: Legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 12f

        generateRandomBarData()
    }

    /**
     * 隨機生成長條圖進度
     */
    private fun generateRandomBarData() {
        val binding = getBinding() ?: return
        val entries = ArrayList<BarEntry>()

        for (i in barMetrics.indices) {
            val achieved = Random.nextInt(35, 95).toFloat()
            val remaining = (100f - achieved).coerceAtLeast(0f)
            entries.add(BarEntry(i.toFloat(), floatArrayOf(achieved, remaining)))
        }

        val dataSet = BarDataSet(entries, "").apply {
            stackLabels = arrayOf("已達成", "未達成")
            colors = listOf(
                Color.parseColor("#4CAF50"), // 翠綠 (達成)
                Color.parseColor("#90A4AE")  // 柔和灰藍 (剩餘)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 10) "${value.toInt()}%" else ""
                }
            }
        }

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSet)

        val barData = BarData(dataSets).apply {
            barWidth = 0.65f
        }

        binding.horizontalBarChart.data = barData
        binding.horizontalBarChart.animateXY(800, 800, Easing.EaseInOutQuad)
        binding.horizontalBarChart.invalidate()
    }

    /**
     * 初始化雷達圖容器
     */
    private fun initRadarViews() {
        val binding = getBinding() ?: return

        if (radarView == null) {
            radarView = RadarView(requireContext())
            binding.FrameLayoutR1.removeAllViews()
            binding.FrameLayoutR1.addView(radarView)
        }

        if (radar2View == null) {
            radar2View = Radar2View(requireContext())
            binding.FrameLayoutR2.removeAllViews()
            binding.FrameLayoutR2.addView(radar2View)

            // 點擊六維雷達圖區域也可直接切換數值
            binding.FrameLayoutR2.setOnClickListener {
                randomizeRadarData()
            }
        }
    }

    /**
     * 隨機變更五維與六維雷達圖數值
     */
    private fun randomizeRadarData() {
        // 1. 五維技能圖隨機
        val p5 = Array<Double?>(5) { Random.nextDouble(0.3, 1.0) }
        radarView?.setPercents(p5)

        // 2. 六維戰力圖隨機
        val p6 = Array<Double?>(6) { Random.nextDouble(0.2, 1.0) }
        radar2View?.setPercents(p6)
    }
}