package com.example.lin_new_project_kotlin_ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.example.lin_new_project_kotlin_ai.databinding.ActivityMainBinding
import com.example.lin_new_project_kotlin_ai.`fun`.LanguageUtils
import com.example.lin_new_project_kotlin_ai.`fun`.MsgEvent
import com.example.lin_new_project_kotlin_ai.`fun`.RxBus
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingActivity
import com.example.lin_new_project_kotlin_ai.webService.WebApi
import com.google.zxing.integration.android.IntentIntegrator
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseBindingActivity<ActivityMainBinding?>() {
    var navController: NavController? = null
    @JvmField
    var currentFragment: String = "HomeFragment"
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        EdgeToEdge.enable(this);
        WebApi.initialization(this)
        mainActivity = this
        requestPermission()
        initView()
    }


    private fun initView() {
        navController = findNavController(this, R.id.mainNavigation)
    }

    fun pageChangeNavController(directions: NavDirections, tag: Int) {
        try {
            Log.d("切換Fragment", "TAG:" + tag + "\naaaa:")
            mainActivity!!.navController!!.navigate(directions)
        } catch (e: Exception) {
            Log.d("切換Fragment", "TAG:" + tag + "\nErr:" + e)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (currentFragment != "HomeFragment") {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                navController!!.popBackStack()
                return false // 返回false让事件继续传递到onBackPressed
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (currentFragment == "NfcFragment") {
            RxBus.getInstance().post(MsgEvent(MyApplication.nfc, intent))
        }
    }

    fun requestPermission() {
        // 如果裝置版本是6.0（包含）以上


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            val hasPermission1 = checkSelfPermission(
                Manifest.permission.CAMERA
            )


            val hasPermission2 = checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            val hasPermission3 = checkSelfPermission(
                Manifest.permission.INTERNET
            )

            val hasPermission4 = checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            val hasPermission5 = checkSelfPermission(
                Manifest.permission.GET_ACCOUNTS
            )

            val hasPermission6 = checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            val hasPermission7 = checkSelfPermission(
                Manifest.permission.ACCESS_NETWORK_STATE
            )

            val hasPermission8 = checkSelfPermission(
                Manifest.permission.ACCESS_WIFI_STATE
            )

            val hasPermission9 = checkSelfPermission(
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            )
            val hasPermission10 = checkSelfPermission(
                Manifest.permission.NFC
            )
            val hasPermission11 = checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            )
            val hasPermission12 = checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val hasPermission13 = checkSelfPermission(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
            val hasPermission14 = checkSelfPermission(
                Manifest.permission.BLUETOOTH_SCAN
            )
            val hasPermission15 = checkSelfPermission(
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            val hasPermission16 = checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            )
            val hasPermission17 = checkSelfPermission(
                Manifest.permission.BLUETOOTH
            )
            val hasPermission18 = checkSelfPermission(
                Manifest.permission.REQUEST_INSTALL_PACKAGES
            )
            // 如果未授權
            if (hasPermission1 != PackageManager.PERMISSION_GRANTED || hasPermission2 != PackageManager.PERMISSION_GRANTED || hasPermission3 != PackageManager.PERMISSION_GRANTED || hasPermission4 != PackageManager.PERMISSION_GRANTED || hasPermission5 != PackageManager.PERMISSION_GRANTED || hasPermission6 != PackageManager.PERMISSION_GRANTED || hasPermission7 != PackageManager.PERMISSION_GRANTED || hasPermission8 != PackageManager.PERMISSION_GRANTED || hasPermission9 != PackageManager.PERMISSION_GRANTED || hasPermission10 != PackageManager.PERMISSION_GRANTED || hasPermission11 != PackageManager.PERMISSION_GRANTED || hasPermission12 != PackageManager.PERMISSION_GRANTED || hasPermission13 != PackageManager.PERMISSION_GRANTED || hasPermission14 != PackageManager.PERMISSION_GRANTED || hasPermission15 != PackageManager.PERMISSION_GRANTED || hasPermission16 != PackageManager.PERMISSION_GRANTED || hasPermission17 != PackageManager.PERMISSION_GRANTED || hasPermission18 != PackageManager.PERMISSION_GRANTED


            ) {
                // 請求授權
                // 第一個參數是請求授權的名稱
                // 第二個參數是請求代碼

                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.NFC,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.REQUEST_INSTALL_PACKAGES,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE

                    ),
                    100
                )
            }
        }
    }

    fun commonROMPermissionCheck(context: Context?): Boolean {
        var result: Boolean? = true
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean?
            } catch (e: Exception) {
                Log.e("TAG", Log.getStackTraceString(e))
            }
        }
        return result!!
    }

    //申請權限
    fun requestAlertWindowPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.setData(Uri.parse("package:" + getPackageName()))
        startActivityForResult(intent, IntentIntegrator.REQUEST_CODE)
    }

    companion object {
        //416370
        @JvmField
        var mainActivity: MainActivity? = null
    }
}