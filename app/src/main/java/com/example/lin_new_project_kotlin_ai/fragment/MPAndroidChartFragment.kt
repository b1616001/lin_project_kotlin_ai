package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.example.lin_new_project_kotlin_ai.databinding.FragmentMpandroidChartBinding
import com.example.lin_new_project_kotlin_ai.`fun`.PieChartMethod
import com.example.lin_new_project_kotlin_ai.view.Radar2View
import com.example.lin_new_project_kotlin_ai.view.RadarView
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate


class MPAndroidChartFragment : BaseBindingFragment<FragmentMpandroidChartBinding?>() {
    private var radar2View: Radar2View? = null
    var radarView: RadarView? = null
        get() {
            if (radar2View == null) field = RadarView(requireContext())
            return field
        }
        private set
    private val booleanStackedBar = false
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentMpandroidChartBinding {
        return FragmentMpandroidChartBinding.inflate(layoutInflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        initPieChart()
        getBinding()!!.btnRadarChart.setOnClickListener { view: View? ->
            Invisble()
            getBinding()!!.linearLayoutRadarChart.setVisibility(View.VISIBLE)
            getBinding()!!.FrameLayoutR1.removeAllViews()
            getBinding()!!.FrameLayoutR2.removeAllViews()
            getBinding()!!.FrameLayoutR1.addView(this.radarView)
            getBinding()!!.FrameLayoutR2.addView(getRadar2View())
        }
        getBinding()!!.btnChart.setOnClickListener { view: View? ->
            Invisble()
            getBinding()!!.linearLayoutChart.setVisibility(View.VISIBLE)
        }
        getBinding()!!.btnStackedBar.setOnClickListener { view: View? ->
            Invisble()
            getBinding()!!.linearLayoutStackedBar.setVisibility(View.VISIBLE)
            if (!booleanStackedBar) {
                initStackedBar()
            }
        }
    }


    private fun initPieChart() {
        val arrayList = ArrayList<String?>()
        val yValueList = ArrayList<Int?>()
        arrayList.add("bb")
        arrayList.add("aa")
        arrayList.add("cc")
        yValueList.add(10)
        yValueList.add(20)
        yValueList.add(50)
        PieChartMethod.ini(getBinding()!!.PieChartC1, true) //初始化PieChart 設定
        PieChartMethod.setData(getContext(), getBinding()!!.PieChartC1,
            arrayList, yValueList)

    }




    private fun initStackedBar() {
        getBinding()!!.horizontalBarChart.setHighlightPerDragEnabled(true)
        getBinding()!!.horizontalBarChart.setDrawBarShadow(false)
        getBinding()!!.horizontalBarChart.setDrawValueAboveBar(false)
        //horizontalBarChart.setContentDescription("××表");
        getBinding()!!.horizontalBarChart.getDescription().setEnabled(false)
        getBinding()!!.horizontalBarChart.setNoDataText("142423")
        getBinding()!!.horizontalBarChart.setDragEnabled(false)
        getBinding()!!.horizontalBarChart.setPinchZoom(false) //scaling can now only be done on x- and y-axis separately
        getBinding()!!.horizontalBarChart.setDrawGridBackground(false)
        getBinding()!!.horizontalBarChart.getAxisRight().setEnabled(false) //不绘制右侧轴线
        //        horizontalBarChart.getAxisLeft().setEnabled(false);
        getBinding()!!.horizontalBarChart.setTouchEnabled(false) // 設定是否可以觸控
        getBinding()!!.horizontalBarChart.setDragEnabled(false) // 是否可以拖拽
        getBinding()!!.horizontalBarChart.setScaleEnabled(false) // 是否可以縮放
        getBinding()!!.horizontalBarChart.setPinchZoom(false) //y軸的值是否跟隨圖表變換縮放;如果禁止，y軸的值會跟隨圖表變換縮放
        val gridview_params: LinearLayout.LayoutParams =
            getBinding()!!.horizontalBarChart.getLayoutParams() as LinearLayout.LayoutParams
        gridview_params.height = 50 * 100
        val datamm = ArrayList<String?>()
        for (i in 0..49) {
            datamm.add("ca" + (i + 1))
        }

        val xl: XAxis = getBinding()!!.horizontalBarChart.getXAxis()
        xl.setValueFormatter(MyXFormatter(datamm))
        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        //xl.setLabelRotationAngle(45);  //标签倾斜
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.setCenterAxisLabels(false) //可不加这句，默认为false
        //xl.setGranularity(sCount);  //x轴坐标占的宽度
        xl.setGranularity(1f) //x轴坐标占的宽度
        //xl.setCenterAxisLabels(true);
//        xl.setAxisMinimum(10f); // 此轴显示的最小值
//        xl.setAxisMaximum(-0.3f); // 此轴显示的最大值
        xl.setLabelCount(50) //显示的坐标数量


        val yl: YAxis = getBinding()!!.horizontalBarChart.getAxisLeft()
        yl.setDrawAxisLine(true)
        //        yl.setAxisMaximum(0.5f);
//        yl.setAxisMaximum(1f);
        yl.setDrawGridLines(true)
        yl.setAxisMinimum(0f) //this replaces setStartAtZero(true)
        //        yl.setPosition(YAxis.Axi);
        yl.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //        float[] val = {54,24, 35, 73.6f, 94.2f, 23, 6, 86, 55, 44.4f, 77.77f, 0, 33,24, 35, 73.6f, 94.2f, 23, 6, 86, 55, 44.4f, 77.77f, 0, 33};
        val vv = floatArrayOf(
            54f,
            24f,
            35f,
            73.6f,
            94.2f,
            23f,
            6f,
            86f,
            55f,
            44.4f,
            77.77f,
            0f,
            33f,
            24f,
            35f,
            73.6f,
            94.2f,
            23f,
            6f,
            86f,
            55f,
            44.4f,
            77.77f,
            0f,
            33f,
            54f,
            24f,
            35f,
            73.6f,
            94.2f,
            23f,
            6f,
            86f,
            55f,
            44.4f,
            77.77f,
            0f,
            33f,
            24f,
            35f,
            73.6f,
            94.2f,
            23f,
            6f,
            86f,
            55f,
            44.4f,
            77.77f,
            0f,
            33f
        )

        setData(50, vv)
        getBinding()!!.horizontalBarChart.setFitBars(false)
        getBinding()!!.horizontalBarChart.animateXY(2000, 2000)

        val legend: Legend = getBinding()!!.horizontalBarChart.getLegend()
        legend.setEnabled(false) //不显示图例
    }

    private fun Invisble() {
        getBinding()!!.linearLayoutRadarChart.setVisibility(View.INVISIBLE)
        getBinding()!!.linearLayoutChart.setVisibility(View.INVISIBLE)
        getBinding()!!.linearLayoutStackedBar.setVisibility(View.INVISIBLE)
    }

    fun getRadar2View(): Radar2View? {
        if (radar2View == null) {
            radar2View = Radar2View(requireContext())
            getBinding()!!.FrameLayoutR2.setOnClickListener { view: View? ->
                val percents = arrayOfNulls<Double>(6)
                for (i in 0..5) {
                    percents[i] = Math.random()
                }
                radar2View!!.setPercents(percents)
            }
        }
        return radar2View
    }

    private fun setData(count: Int, `val`: FloatArray) {
        val barWidth = 0.8f //每个彩色数据条的宽度
        val spaceForBar = 1f //每个数据条实际占的宽度
        val yVals1: ArrayList<BarEntry?> = ArrayList<BarEntry?>()

        for (i in 0..<count) {
            //float val = (float) (Math.random() * range);
//            yVals1.add(new BarEntry(i * spaceForBar, val[i], getResources().getDrawable(R.drawable.ic_menu_camera)));

            yVals1.add(BarEntry(i * spaceForBar, floatArrayOf(`val`[i], 100 - `val`[i])))
        }

        val set1: BarDataSet

        if (getBinding()!!.horizontalBarChart.getData() != null && getBinding()!!.horizontalBarChart.getData()
                .getDataSetCount() > 0
        ) {
            set1 = getBinding()!!.horizontalBarChart.getData().getDataSetByIndex(0) as BarDataSet
            set1.setValues(yVals1)
            getBinding()!!.horizontalBarChart.getData().notifyDataChanged()
            getBinding()!!.horizontalBarChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(yVals1, "XXXX")
            set1.setColors(*this.colors)
            set1.setDrawIcons(false)

            val dataSets: ArrayList<IBarDataSet?> = ArrayList<IBarDataSet?>()
            dataSets.add(set1)

            val data: BarData = BarData(dataSets)
            data.setValueTextSize(10f)

            data.setBarWidth(barWidth)
            getBinding()!!.horizontalBarChart.setData(data)
            getBinding()!!.horizontalBarChart.invalidate()
        }
    }

    private val colors: IntArray
        get() {
            val stacksize = 2

            // have as many colors as stack-values per entry
            val colors = IntArray(stacksize)

            for (i in colors.indices) {
                colors[i] = ColorTemplate.MATERIAL_COLORS[i]
            }

            return colors
        }


    inner class MyXFormatter(private val mValues: MutableList<String?>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String? {
            Log.d("dddd", value.toString())
            if ((value.toInt() >= 0 && (value).toInt() < mValues.size)) {
                return mValues.get(value.toInt())
            } else {
                return ""
            }
        }
    }
}