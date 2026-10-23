package com.example.lin_new_project_kotlin_ai.`fun`

import android.annotation.SuppressLint
import android.app.backup.BackupManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.google.gson.Gson
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class CommonUtils {


    @RequiresApi(api = Build.VERSION_CODES.P)
    fun getLocalVersionCode(): Long {
        var localVersion: Long = 0
        try {
            val packageInfo: PackageInfo = MyApplication.getInstance()
                .getPackageManager()
                .getPackageInfo(MyApplication.getInstance().getPackageName(), 0)
            localVersion = packageInfo.getLongVersionCode()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return localVersion
    }

    /**
     * 獲取apk的包名
     */
    fun getApkPackageName(context: Context, apkPath: String): String? {
        val pm = context.getPackageManager()
        val info = pm.getPackageArchiveInfo(apkPath, 0)
        if (info != null) {
            return info.packageName
        } else {
            return null
        }
    }

    companion object {
        private const val MIN_DELAY_TIME = 500 // 兩次點選間隔不能少於1000ms
        private const val lastClickTime: Long = 0

        @JvmStatic
        fun checkStr(str: String?): Boolean {
            return "" != str && str != null
        }


        /**
         * 判斷網絡是否連接
         * 
         * @param context context
         * @return 判斷網絡是否連接
         */
        @JvmStatic
        fun isConnected(context: Context): Boolean {
            val connectivityManager = context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (connectivityManager == null) {
                return false
            }
            @SuppressLint("MissingPermission") val networkInfo =
                connectivityManager.getActiveNetworkInfo()
            return networkInfo != null
        }


        //
        fun getDrawableByVERSION(id: Int): Drawable? {
            //會根據Android 版本來抓取圖片-----------------------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return MyApplication.getInstance().getResources()
                    .getDrawable(id, MyApplication.getInstance().getTheme())
            } else {
                return MyApplication.getInstance().getResources().getDrawable(id)
            }
        }

        @JvmStatic
        fun getColorByVERSION(id: Int): Int {
            //會根據Android 版本來抓取顏色-----------------------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return MyApplication.getInstance().getResources()
                    .getColor(id, MyApplication.getInstance().getTheme())
            } else {
                return MyApplication.getInstance().getResources().getColor(id)
            }
        }

        @JvmStatic
        fun getResourcesString(strid: Int): String {
//        Resources res = getInstance().getResources();
//        Configuration conf = res.getConfiguration();
//        conf.setLocale(Locale.SIMPLIFIED_CHINESE);
//        Context context = getInstance().createConfigurationContext(conf);
//        Resources resources = context.getResources();
//        return resources.getString(strid);
            return MyApplication.getInstance().getResources().getString(strid)
        }

        fun getResourcesIdentifier_drawable(id: String?): Int {
            val resId = MyApplication.getInstance().getResources().getIdentifier(
                id, "drawable", MyApplication.getInstance().getPackageName()
            )
            return resId
        }


        fun getListIntRange(startIndex: Int, endIndex: Int): ArrayList<String?> {
            val arrayList = ArrayList<String?>()
            for (i in startIndex..endIndex) {
                arrayList.add(i.toString())
            }
            return arrayList
        }


        /**
         * 保留小數，不四捨五入
         * 
         * @param value 數值
         * @param keep  保留位數
         * @return string
         */
        fun formatDecimal(value: Double, keep: Int): String {
            val symbols = DecimalFormatSymbols(Locale.US)
            val format = DecimalFormat()
            format.setDecimalFormatSymbols(symbols)
            format.setMaximumFractionDigits(keep)
            format.setGroupingSize(0)
            format.setRoundingMode(RoundingMode.FLOOR)
            return format.format(value)
        }

        /**
         * 保留小數，不四捨五入
         * 
         * @param Svalue 數值
         * @param keep   保留位數
         * @return string
         */
        fun formatDecimal(Svalue: String, keep: Int): String {
            val value = Svalue.toDouble()
            val format = DecimalFormat()
            format.setMaximumFractionDigits(keep)
            format.setGroupingSize(0)
            format.setRoundingMode(RoundingMode.FLOOR)
            return format.format(value)
        }

        fun formatDecimal2(value: Double, keep: Int): String {
            val format = DecimalFormat()
            format.setMaximumFractionDigits(keep)
            format.setGroupingSize(0)
            format.setRoundingMode(RoundingMode.FLOOR)
            return format.format(value)
        }


        fun changeLanguageSetting(language: Locale /*Locale.GERMAN*/) {
            try {
                val activityManagerNative = Class.forName("android.app.ActivityManager")
                val am = activityManagerNative.getMethod("getService").invoke(activityManagerNative)
                val config =
                    am!!.javaClass.getMethod("getConfiguration").invoke(am) as Configuration?

                //            config.setLocale(language);
                config!!.setLocales(
                    LocaleList(
                        language,
                        Locale("en", "US")
                    )
                ) // <<<<<<<<<<<<<<< 切換語言時  ,加入第二語言 英文，避免netflix變中文

                config.javaClass.getDeclaredField("userSetLocale").setBoolean(config, true)
                am.javaClass.getMethod("updatePersistentConfiguration", Configuration::class.java)
                    .invoke(am, config)
                BackupManager.dataChanged("com.android.providers.settings")

                Log.d("多國語系", "success!" + language)


                //PS 如果重開機後語言回歸 ，可以使用updateLanguage
            } catch (e: Exception) {
                Log.d("多國語系", "error-->:" + Gson().toJson(e))
            }
        }
    }
}
