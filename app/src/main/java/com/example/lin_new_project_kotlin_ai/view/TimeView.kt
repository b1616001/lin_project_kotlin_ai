package com.example.lin_new_project_kotlin_ai.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.example.lin_new_project_kotlin_ai.adapter.TimeAdapter
import com.example.lin_new_project_kotlin_ai.databinding.ViewTimeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.TimeMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseView
import java.util.Calendar

class TimeView : BaseView<ViewTimeBinding?>, View.OnClickListener {
    private var context: Context? = null
    var str_time: String = "" //點選時間
    @JvmField
    var str_years: String = "" //年月
    private var aBoolean = false //有沒有手勢移動
    @JvmField
    var arrayList: ArrayList<String?>? = null
    private var timeAdapter: TimeAdapter? = null
    private var lastX = 0
    var hashMap: HashMap<String?, Boolean?>? = HashMap<String?, Boolean?>()

    public override fun onCreateViewBinding(layoutInflater: LayoutInflater): ViewTimeBinding {
        return ViewTimeBinding.inflate(layoutInflater)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.context = context
        getBinding()!!.gvTime.setSelector(ColorDrawable(Color.TRANSPARENT)) //取消預設點擊
    }

    fun setTime(time: String) {
        str_time = time
        val mm = str_time.indexOf("月")
        str_years = str_time.substring(0, mm + 1).trim { it <= ' ' }
        getBinding()!!.tvLeft.setOnClickListener(this)
        getBinding()!!.tvRight.setOnClickListener(this)
        setTimDisplay() //顯示點選時間
        monthChange(0)
    }

    override fun onClick(view: View) {
        if (view == getBinding()!!.tvLeft) {
            monthChange(-1)
            return
        }
        if (view == getBinding()!!.tvRight) {
            monthChange(1)
            return
        }
    }

    fun monthChange(change: Int) {
        try {
            aBoolean = false
            val calendar = Calendar.getInstance()
            calendar.setTime(TimeMethod.stringToDate(str_years, TimeMethod.formatType5))
            calendar.add(Calendar.MONTH, change)
            val firstIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 //0代表周日 6代表周6
            str_years = TimeMethod.dateToString(calendar.getTime(), TimeMethod.formatType5)
            setLog(
                "時間格式->" + "周" + firstIndex + "," + calendar.get(Calendar.DAY_OF_MONTH) + "號,一共有" + calendar.getActualMaximum(
                    Calendar.DAY_OF_MONTH
                ) + "天,年月" + str_years
            )
            getBinding()!!.tvTime.setText(str_years)
            arrayList = ArrayList<String?>()
            for (i in 0..<firstIndex) { //週日不用加空白 周一到周六每一天追加一個空白
                arrayList!!.add("")
            }
            for (i in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                if (i < 10) {
                    arrayList!!.add("0" + i)
                } else {
                    arrayList!!.add(i.toString() + "")
                }
            }
            setLog("hashMap->" + hashMap)
            timeAdapter = TimeAdapter(context, arrayList, str_time, str_years, hashMap)
            getBinding()!!.gvTime.setAdapter(timeAdapter)
            getBinding()!!.gvTime.setOnTouchListener(object : OnTouchListener{
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    val action = event.getAction()
                    when (action) {
                        MotionEvent.ACTION_DOWN ->
                            //   Log.d(TAG, "onTouchEvent: down height="+ getHeight());
                            lastX = event.getRawX().toInt()

                        MotionEvent.ACTION_UP -> {
                            val xx = lastX - event.getRawX().toInt()
                            setLog("移動的值" + xx + "," + aBoolean)
                            if (!aBoolean) {
                                return false
                            }
                            if (xx == 0 || (0 > xx && xx > -30) || (0 < xx && xx < 30)) {
                                return false
                            } else {
                                if (xx > 0) {
                                    //往左
                                    setLog("移動的值" + "往左")
                                    monthChange(-1)
                                } else {
                                    //往右
                                    setLog("移動的值" + "往右")
                                    monthChange(1)
                                }
                                return true
                            }
                        }

                        MotionEvent.ACTION_MOVE -> aBoolean = true
                    }
                    return false
                }
            })
//            getBinding()!!.gvTime.setOnTouchListener({ v, event ->
//                val action = event.getAction()
//                when (action) {
//                    MotionEvent.ACTION_DOWN ->
//                        //   Log.d(TAG, "onTouchEvent: down height="+ getHeight());
//                        lastX = event.getRawX() as Int
//
//                    MotionEvent.ACTION_UP -> {
//                        val xx = lastX - event.getRawX() as Int
//                        setLog("移動的值" + xx + "," + aBoolean)
//                        if (!aBoolean) {
//                            return@setOnTouchListener false
//                        }
//                        if (xx == 0 || (0 > xx && xx > -30) || (0 < xx && xx < 30)) {
//                            return@setOnTouchListener false
//                        } else {
//                            if (xx > 0) {
//                                //往左
//                                setLog("移動的值" + "往左")
//                                monthChange(-1)
//                            } else {
//                                //往右
//                                setLog("移動的值" + "往右")
//                                monthChange(1)
//                            }
//                            return@setOnTouchListener true
//                        }
//                    }
//
//                    MotionEvent.ACTION_MOVE -> aBoolean = true
//                }
//                false
//            })

        } catch (e: Exception) {
        }
    }

    private fun setTimDisplay() {
        try {
            val calendar = Calendar.getInstance()
            calendar.setTime(TimeMethod.stringToDate(str_time, TimeMethod.formatType6))
            val firstIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
            setLog(
                "時間格式" + "周" + firstIndex + "," + calendar.get(Calendar.DAY_OF_MONTH) + "號,一共有" + calendar.getActualMaximum(
                    Calendar.DAY_OF_MONTH
                ) + "天"
            )
            val ii = str_time.indexOf("年")
            val mm = str_time.indexOf("日")
            getBinding()!!.tvYy.setText(str_time.substring(0, ii + 1))
            var qq = ""
            when (firstIndex) {
                1 -> qq = "  周一"
                2 -> qq = "  周二"
                3 -> qq = "  周三"
                4 -> qq = "  周四"
                5 -> qq = "  周五"
                6 -> qq = "  周六"
                0 -> qq = "  周日"
            }
            getBinding()!!.tvMm.setText(str_time.substring(ii + 1, mm + 1) + qq)
        } catch (e: Exception) {
        }
    }

    fun setValue(tt: String) {
        str_time = tt
        timeAdapter!!.setStr_time(tt)
        timeAdapter!!.notifyDataSetChanged()
        setTimDisplay()
    }

    fun setValue(hashMap: HashMap<String?, Boolean?>?) {
        this.hashMap = hashMap
        timeAdapter!!.setHashMap(hashMap)
        timeAdapter!!.notifyDataSetChanged()
    }
}
