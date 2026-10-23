package com.example.lin_new_project_kotlin_ai.`fun`

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.widget.Toast
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import java.text.DecimalFormat
import java.util.Collections

object PieChartMethod {
    var mParties = arrayOf<String>(
        "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
        "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
        "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
        "Party Y", "Party Z"
    )

    fun setText(mChart: PieChart, text: String?) {
        mChart.setCenterText(text)

//        /**/设置饼图中间的文字 * /
    }

    fun ini(mChart: PieChart, enabled: Boolean) {
        mChart.setUsePercentValues(true) //使用百分比顯示
        mChart.getDescription().setEnabled(false) //设置pieChart图表的描述
        mChart.setBackgroundColor(Color.WHITE) //设置pieChart图表背景色
        mChart.setExtraOffsets(5f, 5f, 5f, 5f) //设置pieChart图表上下左右的偏移，类似于外边距
        mChart.setDragDecelerationFrictionCoef(0.95f) //设置pieChart图表转动阻力摩擦系数[0,1] 减速摩擦系数,值越大表示会缓慢停止
        mChart.setRotationAngle(0f) //设置pieChart图表起始角度
        mChart.setRotationEnabled(false) //设置pieChart图表是否可以手动旋转
        mChart.setHighlightPerTapEnabled(true) //设置piecahrt图表点击Item高亮是否可用,这个方法默认是true，设置为false之后，点击每一块不能向外突出
        mChart.animateY(1500, Easing.EaseInOutQuad) //设置pieChart图表展示动画效果

        // 设置 pieChart 图表Item文本属性
        mChart.setDrawEntryLabels(true) //设置pieChart是否只显示饼图上百分比不显示文字（true：下面属性才有效果）
        mChart.setEntryLabelColor(Color.WHITE) //设置pieChart图表文本字体颜色
        mChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD) //设置pieChart图表文本字体样式给字体加粗
        mChart.setEntryLabelTextSize(14f) //设置pieChart图表文本字体大小

        // 设置 pieChart 内部圆环属性
        mChart.setDrawHoleEnabled(enabled) //这个方法为true就是环形图，为false就是饼图
        mChart.setHoleRadius(58f) //设置中间环形的半径
        mChart.setTransparentCircleRadius(61f) //设置半透明圆环的半径,看着就有一种立体的感觉
        mChart.setTransparentCircleColor(Color.WHITE) //设置半透58f-61f明圆环的颜色
        mChart.setTransparentCircleAlpha(110) //设置PieChart内部透明圆与内部圆间距(58f-61f)透明度[0~255]数值越小越透明
        mChart.setHoleColor(Color.WHITE) //设置环形中间空白颜色是白色
        mChart.setDrawCenterText(true) //设置饼图中间是否可以添加文字
        mChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD) //设置所有DataSet内数据实体（百分比）的文本字体样式 字体加粗
        mChart.setCenterText("報表")
//        /**/设置饼图中间的文字 * /
        mChart.setCenterTextSize(20f)
//        /**/设置饼图中间的文字大小 * /
        if (!enabled) {
            mChart.setCenterTextColor(Color.WHITE)
        }
        //        mChart.setDrawSliceText(true);//百分比下的文字說明不顯示
        //--------------------------
        //隱藏顏色說明文
        val l = mChart.getLegend()
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)
        l.setOrientation(Legend.LegendOrientation.VERTICAL)
        l.setDrawInside(false)
        l.setEnabled(false)


        //--------------------------

//        // 获取pieCahrt图列
//        Legend l = mChart.getLegend();
//        l.setEnabled(true);                    //是否启用图列（true：下面属性才有意义）
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//        l.setOrientation(Legend.LegendOrientation.VERTICAL);
//        l.setForm(Legend.LegendForm.DEFAULT); //设置图例的形状
//        l.setFormSize(10);					  //设置图例的大小
//        l.setFormToTextSpace(10f);			  //设置每个图例实体中标签和形状之间的间距
//        l.setDrawInside(false);
//        l.setWordWrapEnabled(true);			  //设置图列换行(注意使用影响性能,仅适用legend位于图表下面)
//        l.setXEntrySpace(10f);				  //设置图例实体之间延X轴的间距（setOrientation = HORIZONTAL有效）
//        l.setYEntrySpace(8f);				  //设置图例实体之间延Y轴的间距（setOrientation = VERTICAL 有效）
//        l.setYOffset(0f);					  //设置比例块Y轴偏移量
//        l.setTextSize(14f);					  //设置图例标签文本的大小
//        l.setTextColor(Color.parseColor("#ff9933"));//设置图例标签文本的颜色
//        /**/pieChart 选择监听 * /
//        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener(){
//            @Override
//            public void onValueSelected(Entry e, Highlight h) {
//
//            }
//
//            @Override
//            public void onNothingSelected() {
//
//            }
//        });
//
//                /**/设置MARKERVIEW * /
//        CustomMarkerView mv = new CustomMarkerView(this, new PercentFormatter());
//        mv.setChartView(mChart);
//        mChart.setMarker(mv);
    }

    //    public static void setData(Context context, PieChart mChart, int count, float range) {
    //
    //        float mult = range;
    //
    //        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
    //         ArrayList arrayList = new ArrayList();
    //        ArrayList<Integer> yValueList = new ArrayList<Integer>();
    //        arrayList.add("bb");
    //        arrayList.add("aa");
    //        arrayList.add("cc");
    //        yValueList.add(10);
    //        yValueList.add(20);
    //        yValueList.add(30);
    //        //注意：将条目添加到entry数组时，其顺序决定了它们在中心周围的位置
    //        for (int i = 0; i < arrayList.size(); i++) {
    //            Log.d("XXXXX", (float) ((Math.random() * mult) + mult / 5) + "");
    //            Log.d("XXXXX", mParties[i % mParties.length] + "");
//    /**/            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5),
//    * /                    mParties[i % mParties.length],
//    * /                    getResources().getDrawable(R.drawable.side_nav_bar)));
//    * /            entries.add(new PieEntry((float) 1.0,
//    * /                    mParties[i % mParties.length])); */
    //            entries.add(new PieEntry((float)yValueList.get(i),
    //                    arrayList.get(i)+""));
    //        }
    //
    //        PieDataSet dataSet = new PieDataSet(entries, "Election Results");
//    /**/        dataSet.setDrawValues(false);
//    * /        dataSet.setDrawIcons(false); */
    //
    //        dataSet.setSliceSpace(0f);
    //        dataSet.setIconsOffset(new MPPointF(0, 40));
    //        dataSet.setSelectionShift(5f);
    //
    //        // add a lot of colors
    //
    //        ArrayList<Integer> colors = new ArrayList<Integer>();
    //
//    /**/        for (int c : ColorTemplate.VORDIPLOM_COLORS)
//    * /            colors.add(c);
//    * /
//    * ////        for (int c : ColorTemplate.JOYFUL_COLORS)
//    * /            colors.add(c);
//    * /
//    * ////        for (int c : ColorTemplate.COLORFUL_COLORS)
//    * /            colors.add(c);
//    * /
//    * ////        for (int c : ColorTemplate.LIBERTY_COLORS)
//    * /            colors.add(c);
//    * /
//    * ////        for (int c : ColorTemplate.PASTEL_COLORS)
//    * /            colors.add(c);
//    * /
//    * ////        colors.add(Color.WHITE);
//    * /        colors.add(ColorTemplate.getHoloBlue()); */
    //
    //        colors.add(Color.BLUE);
    //        colors.add(Color.BLACK);
    //        colors.add(Color.GRAY);
    //        dataSet.setColors(colors);
    //        //dataSet.setSelectionShift(0f);
    //
    //
    //
    //
    //        PieData com.example.lin_new_project_kotlin.data = new PieData(dataSet);
    //        com.example.lin_new_project_kotlin.data.setValueFormatter(new PercentFormatter());
    //        com.example.lin_new_project_kotlin.data.setValueTextSize(11f);//百分比文字大小
    //        com.example.lin_new_project_kotlin.data.setValueTextColor(Color.WHITE);
//    /**/        com.example.lin_new_project_kotlin.data .setDrawValues(false);//百分比不顯示 */ //
//    /**/        com.example.lin_new_project_kotlin.data .setValueTypeface(Typeface.createFromFile("Cxcxvvxb")); */ //        mChart.setData(com.example.lin_new_project_kotlin.data);
    //
    //        // undo all highlights
    //        mChart.highlightValues(null);
    //
    //        mChart.invalidate();
    //    }
    fun setData(
        context: Context?,
        mChart: PieChart,
        hashMap: HashMap<String?, Int?>,
        sort: String
    ) {
        val list: List<Map.Entry<String, Int>>
        if (sort == "星期") {
            list = getSort2(hashMap.entries as Set<Map.Entry<String, Int>>)
        } else {
            list = hashMap.entries.toList() as List<Map.Entry<String, Int>> //轉換為list
        }
        val colors = ArrayList<Int?>()
        colors.add(Color.parseColor("#E38EFF"))
        colors.add(Color.parseColor("#99DD00"))
        colors.add(Color.parseColor("#FF0000"))
        colors.add(Color.parseColor("#003377"))
        colors.add(Color.parseColor("#0066FF"))
        colors.add(Color.parseColor("#0000FF"))
        colors.add(Color.parseColor("#CC00FF"))

        val entries = ArrayList<PieEntry?>()
        for (map in list) {
            val pieEntry = PieEntry(map.value!!.toFloat(), map.key)
            entries.add(pieEntry)
        }
        //            for (String key:hashMap.keySet()){
//            PieEntry pieEntry = new PieEntry(hashMap.get(key), key);
//            entries.add(pieEntry);
//        }
        //饼状图数据集 PieDataSet
        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.setDrawValues(true)
        pieDataSet.setDrawIcons(true)
        pieDataSet.setIconsOffset(MPPointF(0f, 40f))
        pieDataSet.setSliceSpace(0f) //设置饼状Item之间的间隙
        pieDataSet.setSelectionShift(0f) //设置饼状Item被选中时变化的距离
        pieDataSet.setColors(colors) //为DataSet中的数据匹配上颜色集(饼图Item颜色)

        //最终数据 PieData
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true) //设置是否显示数据实体(百分比，true:以下属性才有意义)
        pieData.setValueTextColor(Color.WHITE) //设置所有DataSet内数据实体（百分比）的文本颜色
        pieData.setValueTextSize(12f) //设置所有DataSet内数据实体（百分比）的文本字体大小
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD) //设置所有DataSet内数据实体（百分比）的文本字体样式
        pieData.setValueFormatter(MyValueFormatter()) //设置所有DataSet内数据实体（百分比）的文本字体格式
        mChart.setData(pieData)
        mChart.highlightValues(null)
        mChart.invalidate() //将图表重绘以显示设置的属性和数据
    }

    private fun getSort2(entrySet: Set<Map.Entry<String, Int>>):
            List<Map.Entry<String, Int>> {
        val hashMap = HashMap<String?, Int?>()
        hashMap.put("周一", 1)
        hashMap.put("周二", 2)
        hashMap.put("周三", 3)
        hashMap.put("周四", 4)
        hashMap.put("周五", 5)
        hashMap.put("周六", 6)
        hashMap.put("周日", 7)
        val list: List<Map.Entry<String, Int>> =entrySet.toList()
        Collections.sort(list, object :Comparator<Map.Entry<String, Int>>{
            override fun compare(
                o1: Map.Entry<String, Int>,
                o2: Map.Entry<String, Int>
            ): Int {
                try {
                    val a1: Int = hashMap.get(o1.key)!!
                    val a2: Int = hashMap.get(o2.key)!!
                    if (a1 > a2) {
                        return 1
                    }
                    return -1
                } catch (e: Exception) {
                    return -1
                }
            }
        })

        return list
    }




    fun setData(context: Context?, mChart: PieChart, count: Int, range: Float) {
        val pieEntryList = ArrayList<PieEntry?>()
        val colors = ArrayList<Int?>()
        colors.add(Color.parseColor("#f17548"))
        colors.add(Color.parseColor("#FF9933"))
        //饼图实体 PieEntry
        val CashBalance = PieEntry(70f, "现金余额 1500")
        val ConsumptionBalance = PieEntry(30f, "消费余额 768")
        pieEntryList.add(CashBalance)
        pieEntryList.add(ConsumptionBalance)

        //饼状图数据集 PieDataSet
        val pieDataSet = PieDataSet(pieEntryList, "资产总览")
        pieDataSet.setSliceSpace(3f) //设置饼状Item之间的间隙
        pieDataSet.setSelectionShift(10f) //设置饼状Item被选中时变化的距离
        pieDataSet.setColors(colors) //为DataSet中的数据匹配上颜色集(饼图Item颜色)
        //最终数据 PieData
        val pieData = PieData(pieDataSet)

        pieData.setDrawValues(false) //设置是否显示数据实体(百分比，true:以下属性才有意义)
        pieData.setValueTextColor(Color.BLUE) //设置所有DataSet内数据实体（百分比）的文本颜色
        pieData.setValueTextSize(12f) //设置所有DataSet内数据实体（百分比）的文本字体大小
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD) //设置所有DataSet内数据实体（百分比）的文本字体样式
        pieData.setValueFormatter(MyValueFormatter()) //设置所有DataSet内数据实体（百分比）的文本字体格式
        mChart.setData(pieData)
        mChart.highlightValues(null)
        mChart.invalidate() //将图表重绘以显示设置的属性和数据
    }


    fun setData(
        context: Context?,
        mChart: PieChart?,
        arrayList_PieEntry:kotlin.collections.ArrayList<String?>,
        arrayList_Integer:kotlin.collections.ArrayList<Int?>
    ) {
        val entries = ArrayList<PieEntry?>()
        for (i in arrayList_PieEntry.indices) {
            val pieEntry = PieEntry(arrayList_Integer.get(i)!!.toFloat(), arrayList_PieEntry.get(i))
            entries.add(pieEntry)
        }
        val colors = ArrayList<Int?>()
        colors.add(Color.parseColor("#f17548"))
        colors.add(Color.parseColor("#FF9933"))
        colors.add(Color.BLUE)
        colors.add(Color.BLACK)
        colors.add(Color.GRAY)
        //饼状图数据集 PieDataSet
        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.setDrawValues(true)
        pieDataSet.setDrawIcons(true)
        pieDataSet.setIconsOffset(MPPointF(0f, 40f))
        pieDataSet.setSliceSpace(0f) //设置饼状Item之间的间隙
        pieDataSet.setSelectionShift(0f) //设置饼状Item被选中时变化的距离
        pieDataSet.setColors(colors) //为DataSet中的数据匹配上颜色集(饼图Item颜色)

        //最终数据 PieData
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(true) //设置是否显示数据实体(百分比，true:以下属性才有意义)
        pieData.setValueTextColor(Color.WHITE) //设置所有DataSet内数据实体（百分比）的文本颜色
        pieData.setValueTextSize(12f) //设置所有DataSet内数据实体（百分比）的文本字体大小
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD) //设置所有DataSet内数据实体（百分比）的文本字体样式
        pieData.setValueFormatter(MyValueFormatter()) //设置所有DataSet内数据实体（百分比）的文本字体格式
        mChart!!.setData(pieData)
        mChart!!.highlightValues(null)
        mChart!!.invalidate() //将图表重绘以显示设置的属性和数据


//        /**/pieChart 选择监听 * /
        mChart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight) {
                Log.d("dddd", "dddddddd" + Gson().toJson(e) + Gson().toJson(h))
                Toast.makeText(
                    context,
                    arrayList_PieEntry.get(
                        h.getX().toInt()
                    ) + arrayList_Integer.get(h.getX().toInt()),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onNothingSelected() {
                Log.d("dddd", "dddddxddd")
            }
        })
    }

    class MyValueFormatter : ValueFormatter() {
        var mFormat: DecimalFormat

        init {
            mFormat = DecimalFormat("###,###,##0.0")
        }

        override fun getFormattedValue(value: Float): String {
            Log.d("getFormattedValue", value.toString() + "")
            return mFormat.format(value.toDouble()) + " %"
        }
    }
}
