package com.example.lin_new_project_kotlin_ai.fragment

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.adapter.HomeAdapter
import com.example.lin_new_project_kotlin_ai.alertDialog.TextAlertDialog
import com.example.lin_new_project_kotlin_ai.databinding.FragmentHomeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.GPSPositioningMethod
import com.example.lin_new_project_kotlin_ai.`fun`.RecyclerViewMethed
import com.example.lin_new_project_kotlin_ai.`fun`.TimeMethod
import com.example.lin_new_project_kotlin_ai.popupwindow.PopupWindowManager
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.example.lin_new_project_kotlin_ai.webService.IWebApiPath
import com.example.lin_new_project_kotlin_ai.webService.WebApi
import com.example.lin_new_project_kotlin_ai.webService.WebApiParameter.getApi_Uid_Parameter
import com.example.lin_new_project_kotlin_ai.webService.data.UidData
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date


class HomeFragment : BaseBindingFragment<FragmentHomeBinding?>() {
    var array_home: Array<String?>? = null
    private var homeAdapter: HomeAdapter? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        array_home = getResources().getStringArray(R.array.home_array)
        initView()
    }

    private fun initView() {
        RecyclerViewMethed.init_VERTICAL(
            getActivity(), getBinding()!!.recyclerView,
            true, 0, CommonUtils.getColorByVERSION(R.color.white), true, true
        )
        homeAdapter = HomeAdapter(getContext(), array_home?: arrayOf() )
        homeAdapter!!.setOnItemClickListener(object:HomeAdapter.OnItemClickListener{
            override fun OnItemClick(string: String?) {
                val index = string!!.indexOf(".")
                if (index == -1) return
                val p = string.substring(0, index)
                when (p) {
                    "1" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToRoomFragment(),
                        1
                    )

                    "2" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToLanguageFragment(),
                        2
                    )

                    "3" -> getGPS()
                    "4" -> getApiUid()
                    "5" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToTtsFragment(),
                        5
                    )

                    "6" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToSignatureFragment(),
                        6
                    )

                    "7" -> setTime()
                    "8" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToTimeFragment(),
                        8
                    )

                    "9" -> arrayData()
                    "10" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToMPAndroidChartFragment(),
                        10
                    )

                    "11" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToViewPagerFragment(),
                        11
                    )

                    "12" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToRecordingFragment(),
                        12
                    )

                    "13" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToNfcFragment(),
                        13
                    )

                    "14" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToSimpleItemFragment(),
                        14
                    )

                    "15" -> if (CommonUtils.isConnected(requireContext())) {
                        showToast("連線中")
                    } else {
                        showToast("無連線")
                    }

                    "16" -> {
                        val textAlertDialog = TextAlertDialog(getContext())
                        textAlertDialog.setOnClickListener(object : TextAlertDialog.OnClickListener{
                            override fun OnClick() {
                                showToast("關閉彈跳視窗")
                            }
                        })
                        textAlertDialog.Show("測試使用AlertDialog", "確定")
                    }

                    "17" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToNotebookFragment(),
                        17
                    )

                    "18" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToTextUrlFragment(),
                        18
                    )

                    "19" -> PopupWindowManager.showTextPopupWindow()
                    "20" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToQrcodeFragment(),
                        20
                    )

                    "21" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToVolumetFragment(),
                        21
                    )

                    "22" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToScreenFragment(),
                        22
                    )

                    "23" -> {
                        mainActivity!!.getWindow().setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                        )
                        //                    mainActivity!!.getSupportActionBar().hide(); //設定隱藏標題
                        val controller: WindowInsetsControllerCompat = WindowCompat.getInsetsController(
                            mainActivity!!.getWindow(),
                            mainActivity!!.getWindow().getDecorView()
                        )
                        if (controller != null) {
                            // 隱藏狀態列
                            controller.hide(WindowInsetsCompat.Type.statusBars())
                            // 設定行為為隱藏後滑動邊緣可暫時顯示（沉浸模式）
                            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
                        }
                    }

                    "24" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToGifFragment(),
                        24
                    )

                    "25" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToWebViewFragment(),
                        25
                    )

                    "26" -> if (!mainActivity!!.commonROMPermissionCheck(getContext())) {
                        mainActivity!!.requestAlertWindowPermission()
                    } else {
                        PopupWindowManager.showMediaMenu(true)
                    }

                    "27" -> {
                        val time = "GMT+8:" + TimeMethod.getTime_zone(
                            TimeMethod.formatType,
                            TimeMethod.zone_GMT_8
                        ) +
                                "\nGMT+9:" + TimeMethod.getTime_zone(
                            TimeMethod.formatType,
                            TimeMethod.zone_GMT_9
                        ) +
                                "\nUTC:" + TimeMethod.getTime_zone(
                            TimeMethod.formatType,
                            TimeMethod.zone_UTC
                        )
                        showToast(time)
                    }

                    "28" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToPublicSpaceFragment(),
                        28
                    )

                    "29" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToGLSurfaceFragment(),
                        29
                    )

                    "30" -> mainActivity!!.pageChangeNavController(
                        HomeFragmentDirections.actionHomeFragmentToScaleImageFragment(),
                        30
                    )
                }            }
        })

        getBinding()!!.recyclerView.setAdapter(homeAdapter)
        //        getBinding().imageView.setOnClickListener(com.example.lin_new_project_kotlin.view ->
//                mainActivity!!.pageChangeNavController(HomeFragmentDirections.actionHomeFragmentToTextFragment(),5)
//        );
    }

    private fun getGPS() {
        val gpsPositioning = GPSPositioningMethod(
            mainActivity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager?,
            mainActivity
        )
        gpsPositioning.startGPS()
        var wgs_x: String? = "0"
        var wgs_y: String? = "0"
        if (gpsPositioning.getMyLongitude() !== 0.0 || gpsPositioning.getMyLatitude() !== 0.0) {
            wgs_x = java.lang.String.valueOf(gpsPositioning.getMyLongitude())
            wgs_y = java.lang.String.valueOf(gpsPositioning.getMyLatitude())
            showToast(" $wgs_x,$wgs_y ")
        } else {
            showToast("定位失敗")
        }
    }
    private fun getApiUid() {
        val time: String = Time()
        val time2 = (time.toInt() * 2895).toString()
        val CHKTOKEN = time2.substring(time2.length - 5)
        val param: MutableMap<String?, Any?>? = getApi_Uid_Parameter(CHKTOKEN)
        WebApi.request(
            WebApi.createApi(IWebApiPath::class.java).apiUid(param),
            object : WebApi.IResponseListener<String?> {
                override fun onSuccess(data: String?) {
                    val searchLoc = data!!.indexOf("{")
                    val searchLoc2 = data!!.lastIndexOf("}") //此字元串中最後一次出現處的索引
                    val dd = data!!.substring(searchLoc, searchLoc2 + 1)
                    val uidData = Gson().fromJson(dd, UidData::class.java)
                    Log.d("WebApi", "onSuccess:" + Gson().toJson(uidData))
                    showToast("WebApi->onSuccess:" + Gson().toJson(uidData))
                }

                override fun onFail() {
                    Log.d("WebApi", "onFail")
                    showToast("WebApi->onFail")
                }
            })
    }

    //    private void showToast(String txt) {
    //        Toast.makeText(mainActivity, txt, Toast.LENGTH_LONG).show();
    //    }
    private fun Time(): String {
        val formatter = SimpleDateFormat("MMdd")
        val curDate = Date(System.currentTimeMillis()) // 獲取當前時間
        val ss = formatter.format(curDate)
        return ss
    }


    private fun arrayData() {
        val Array_A = arrayOf<String?>(
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8",
            "A9", "A10", "A11", "A12", "A13", "A14", "A15"
        )
        val Array_B = arrayOf<String?>("B1", "B2", "B3", "B4", "B5")
        val arrayList: ArrayList<String?> = ArrayList()
        var aa = 0
        var bb = false
        for (i in Array_A.indices) {
            if (i > 0 && i % 3 == 0) {
                arrayList.add(Array_B[aa])
                aa++
            }
            arrayList.add(Array_A[i])
            if (i == Array_A.size - 1) {
                if (Array_A.size % 3 == 0) {
                    bb = true
                }
            }
        }
        if (bb) {
            arrayList.add(Array_B[aa++])
        }


        val Array_C = arrayList.toTypedArray() as Array<String?>
        setLog("混合陣列" + arrayList.toString())
    }

    private fun setTime() {
        try {
            val dd: Date? = TimeMethod.stringToDate("2026-05-22 09:08:25", "yyyy-MM-dd HH:mm")
            setLog("時間格式轉換->dd:" + dd)
            setLog("時間格式轉換->" + TimeMethod.dateToString(dd, "yyyy-MM-dd HH:mm"))
            var Str = "2026-05-22 00:00"
            val Str2 = "2026-05-22 23:59"
            while (Str != Str2) {
                setLog("時間格式轉換前" + Str)
                Str = TimeMethod.addDayMINUTE(Str, 1, "yyyy-MM-dd HH:mm")
                setLog("時間格式轉換後" + Str)
            }
        } catch (e: Exception) {
        }
    }
}