package com.example.lin_new_project_kotlin_ai.popupwindow

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import com.example.lin_new_project_kotlin_ai.MainActivity
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import com.example.lin_new_project_kotlin_ai.window.MediaMenuWindow

/**
 * 優化版 PopupWindowManager
 * - 統一彈窗管理機制 (支援單一當前彈窗堆疊防護、自動清理)
 * - 生命週期感知：自動檢查 Activity 存活狀態 (isFinishing / isDestroyed)，防止 WindowManager 崩潰
 * - 背景暗色遮罩自動聯動：彈窗關閉時保證暗色背景同步銷毀，杜絕殘留遮罩與記憶體洩漏
 * - 完全向下相容所有原有介面 (showTextPopupWindow, showMediaMenu, showAddAccountPopupWindow, showDeleteConfirmPopupWindow)
 */
object PopupWindowManager {
    private const val TAG = "PopupWindowManager"

    private var activePopupWindow: BasePopupWindow<*>? = null
    private var backgroundWindow: BackgroundWindow? = null
    var mediaMenuWindow: MediaMenuWindow? = null

    /**
     * 尋找可用的 DecorView，並確認 Activity 處於正常運作狀態
     */
    fun findDecorView(context: Context?): View? {
        val activity = (context as? Activity) ?: MainActivity.mainActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "findDecorView: Activity is null, finishing, or destroyed.")
            return null
        }
        return activity.window?.decorView
    }

    /**
     * 統一彈窗展示入口
     *
     * @param context Context / Activity
     * @param popup 欲展示的 BasePopupWindow 實例
     * @param showDarkBg 是否展示暗色背景遮罩
     * @param gravity 對齊方向 (預設 Gravity.CENTER)
     * @param x X 軸偏移
     * @param y Y 軸偏移
     * @param onDismiss 彈窗關閉回呼
     * @return 是否成功展示
     */
    fun showPopup(
        context: Context,
        popup: BasePopupWindow<*>,
        showDarkBg: Boolean = true,
        gravity: Int = Gravity.CENTER,
        x: Int = 0,
        y: Int = 0,
        onDismiss: (() -> Unit)? = null
    ): Boolean {
        val decorView = findDecorView(context)
        if (decorView == null) {
            Log.e(TAG, "showPopup failed: cannot retrieve valid decorView.")
            return false
        }

        // 若當前已有彈窗展示，先關閉舊彈窗避免重疊
        dismissCurrentPopup()

        activePopupWindow = popup

        if (showDarkBg) {
            showBg(true)
        }

        popup.setOnCustomDismissListener(object : BasePopupWindow.OnCustomDismissListener {
            override fun onStartDismiss() {}

            override fun onDismiss() {
                if (activePopupWindow == popup) {
                    activePopupWindow = null
                }
                if (showDarkBg) {
                    showBg(false)
                }
                onDismiss?.invoke()
            }
        })

        try {
            popup.showAtLocation(decorView, gravity, x, y)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying popup: ${e.message}", e)
            dismissCurrentPopup()
            return false
        }
    }

    /**
     * 關閉當前展示中的 BasePopupWindow
     */
    fun dismissCurrentPopup() {
        try {
            activePopupWindow?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing active popup: ${e.message}", e)
        } finally {
            activePopupWindow = null
            showBg(false)
        }
    }

    /**
     * 展示通用文字彈窗 (HomeFragment #19)
     */
    fun showTextPopupWindow() {
        val context = MainActivity.mainActivity ?: MyApplication.getInstance().applicationContext
        val popup = TextPopupWindow(context)
        showPopup(context, popup, showDarkBg = true, gravity = Gravity.CENTER)
    }

    /**
     * 新增帳號 PopupWindow (RoomFragment)
     */
    fun showAddAccountPopupWindow(context: Context, onConfirm: (UserData) -> Unit) {
        val popup = AddAccountPopupWindow(context) { userData ->
            onConfirm(userData)
        }
        showPopup(context, popup, showDarkBg = true, gravity = Gravity.CENTER)
    }

    /**
     * 確認刪除 PopupWindow (RoomFragment)
     */
    fun showDeleteConfirmPopupWindow(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val popup = DeleteConfirmPopupWindow(context, title, message) {
            onConfirm()
        }
        showPopup(context, popup, showDarkBg = true, gravity = Gravity.CENTER)
    }

    /**
     * 暗色全螢幕遮罩展示與關閉
     */
    fun showBg(isShow: Boolean) {
        val decorView = findDecorView(MainActivity.mainActivity) ?: return
        if (isShow) {
            if (backgroundWindow == null) {
                backgroundWindow = BackgroundWindow(MyApplication.getInstance(), false, true)
            }
            if (backgroundWindow!!.isShowing) return
            try {
                backgroundWindow!!.showAtLocation(
                    decorView,
                    Gravity.END or Gravity.BOTTOM,
                    0,
                    0
                )
            } catch (e: Exception) {
                Log.e(TAG, "showBg failed: ${e.message}")
            }
        } else {
            try {
                if (backgroundWindow != null && backgroundWindow!!.isShowing) {
                    backgroundWindow!!.dismiss()
                }
            } catch (e: Exception) {
                Log.e(TAG, "dismiss backgroundWindow failed: ${e.message}")
            } finally {
                backgroundWindow = null
            }
        }
    }

    /**
     * 浮動懸浮選單 Window (HomeFragment #26)
     */
    fun showMediaMenu(isShow: Boolean) {
        val activity = MainActivity.mainActivity ?: return
        if (isShow) {
            if (mediaMenuWindow != null) return
            try {
                mediaMenuWindow = MediaMenuWindow(activity)
                mediaMenuWindow!!.setOnCustomDismissListener(object :
                    BaseWindow.OnCustomDismissListener {
                    override fun onDismiss() {
                        mediaMenuWindow = null
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "showMediaMenu failed: ${e.message}", e)
            }
        } else {
            try {
                mediaMenuWindow?.dismiss()
            } catch (e: Exception) {
                Log.e(TAG, "dismiss mediaMenuWindow failed: ${e.message}")
            } finally {
                mediaMenuWindow = null
            }
        }
    }

    /**
     * 清理所有開啟中的彈窗與遮罩 (用於 Activity / Fragment 離開時銷毀)
     */
    fun dismissAll() {
        dismissCurrentPopup()
        showMediaMenu(false)
        showBg(false)
    }
}
