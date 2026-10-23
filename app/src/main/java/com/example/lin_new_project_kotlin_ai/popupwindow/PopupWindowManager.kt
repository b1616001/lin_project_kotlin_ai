package com.example.lin_new_project_kotlin_ai.popupwindow

import android.view.Gravity
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import com.example.lin_new_project_kotlin_ai.window.MediaMenuWindow

object PopupWindowManager {
    private var textPopupWindow: TextPopupWindow? = null
    private var backgroundWindow: BackgroundWindow? = null
    var mediaMenuWindow: MediaMenuWindow? = null
    fun showTextPopupWindow() {
        if (textPopupWindow != null) return
        showBg(true)
        textPopupWindow = TextPopupWindow(MyApplication.getInstance().getApplicationContext())
        textPopupWindow!!.showAtLocation(
            mainActivity!!.getWindow().getDecorView(),
            Gravity.CENTER,
            0,
            0
        )
        textPopupWindow!!.setOnCustomDismissListener(object :
            BasePopupWindow.OnCustomDismissListener {
            override fun onStartDismiss() {
            }

            override fun onDismiss() {
                textPopupWindow = null
                showBg(false)
            }
        })
    }

    fun showBg(isShow: Boolean) {
        if (isShow) {
            if (backgroundWindow == null) {
                backgroundWindow = BackgroundWindow(MyApplication.getInstance(), false, true)
            }
            if (backgroundWindow!!.isShowing()) return
            backgroundWindow!!.showAtLocation(
                mainActivity!!.getWindow().getDecorView(),
                Gravity.END or Gravity.BOTTOM,
                0,
                0
            )
        } else {
            if (backgroundWindow != null) {
                backgroundWindow!!.dismiss()
            }
        }
    }

    fun showMediaMenu(isShow: Boolean) {
        if (isShow) {
            if (mediaMenuWindow != null) return
            mediaMenuWindow = MediaMenuWindow(mainActivity!!)
            mediaMenuWindow!!.setOnCustomDismissListener(object :
                BaseWindow.OnCustomDismissListener{
                override fun onDismiss() {
                    mediaMenuWindow = null
                }
            })
        } else {
            if (mediaMenuWindow != null) {
                mediaMenuWindow!!.dismiss()
                mediaMenuWindow = null
            }
        }
    }
}
