package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.view.Gravity
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import com.example.lin_new_project_kotlin_ai.window.MediaMenuWindow

object PopupWindowManager {
    private var textPopupWindow: TextPopupWindow? = null
    private var backgroundWindow: BackgroundWindow? = null
    private var addAccountPopupWindow: AddAccountPopupWindow? = null
    private var deleteConfirmPopupWindow: DeleteConfirmPopupWindow? = null
    var mediaMenuWindow: MediaMenuWindow? = null

    fun showTextPopupWindow() {
        if (textPopupWindow != null) return
        showBg(true)
        textPopupWindow = TextPopupWindow(MyApplication.getInstance().applicationContext)
        val decorView = mainActivity?.window?.decorView ?: return
        textPopupWindow!!.showAtLocation(
            decorView,
            Gravity.CENTER,
            0,
            0
        )
        textPopupWindow!!.setOnCustomDismissListener(object :
            BasePopupWindow.OnCustomDismissListener {
            override fun onStartDismiss() {}

            override fun onDismiss() {
                textPopupWindow = null
                showBg(false)
            }
        })
    }

    /**
     * 新增帳號 PopupWindow
     */
    fun showAddAccountPopupWindow(context: Context, onConfirm: (UserData) -> Unit) {
        if (addAccountPopupWindow != null) return
        showBg(true)
        val decorView = mainActivity?.window?.decorView ?: return
        addAccountPopupWindow = AddAccountPopupWindow(context) { userData ->
            onConfirm(userData)
        }
        addAccountPopupWindow!!.showAtLocation(
            decorView,
            Gravity.CENTER,
            0,
            0
        )
        addAccountPopupWindow!!.setOnCustomDismissListener(object :
            BasePopupWindow.OnCustomDismissListener {
            override fun onStartDismiss() {}

            override fun onDismiss() {
                addAccountPopupWindow = null
                showBg(false)
            }
        })
    }

    /**
     * 確認刪除 PopupWindow
     */
    fun showDeleteConfirmPopupWindow(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        if (deleteConfirmPopupWindow != null) return
        showBg(true)
        val decorView = mainActivity?.window?.decorView ?: return
        deleteConfirmPopupWindow = DeleteConfirmPopupWindow(context, title, message) {
            onConfirm()
        }
        deleteConfirmPopupWindow!!.showAtLocation(
            decorView,
            Gravity.CENTER,
            0,
            0
        )
        deleteConfirmPopupWindow!!.setOnCustomDismissListener(object :
            BasePopupWindow.OnCustomDismissListener {
            override fun onStartDismiss() {}

            override fun onDismiss() {
                deleteConfirmPopupWindow = null
                showBg(false)
            }
        })
    }

    fun showBg(isShow: Boolean) {
        val decorView = mainActivity?.window?.decorView ?: return
        if (isShow) {
            if (backgroundWindow == null) {
                backgroundWindow = BackgroundWindow(MyApplication.getInstance(), false, true)
            }
            if (backgroundWindow!!.isShowing) return
            backgroundWindow!!.showAtLocation(
                decorView,
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
                BaseWindow.OnCustomDismissListener {
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
