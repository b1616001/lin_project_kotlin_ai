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
import com.example.lin_new_project_kotlin_ai.window.DockCorner
import com.example.lin_new_project_kotlin_ai.window.MediaMenuWindow
import com.example.lin_new_project_kotlin_ai.window.WindowConfigStore
import com.example.lin_new_project_kotlin_ai.window.WindowCustomConfig

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
    @JvmOverloads
    fun showTextPopupWindow(
        context: Context = MainActivity.mainActivity ?: MyApplication.getInstance().applicationContext,
        title: String = "自定義彈窗通知",
        message: String? = null,
        onConfirm: (() -> Unit)? = null
    ) {
        val popup = if (message != null) {
            TextPopupWindow(context, titleText = title, messageText = message, onConfirmAction = onConfirm)
        } else {
            TextPopupWindow(context)
        }
        showPopup(context, popup, showDarkBg = true, gravity = Gravity.CENTER)
    }

    /**
     * GPS 即時定位資訊 PopupWindow
     */
    fun showGpsInfoPopupWindow(context: Context) {
        val popup = GpsInfoPopupWindow(context)
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

    private var windowStateListener: ((WindowCustomConfig) -> Unit)? = null
    private var windowDismissListener: (() -> Unit)? = null

    /**
     * 浮動懸浮選單 Window (向下相容介面)
     */
    fun showMediaMenu(isShow: Boolean) {
        if (isShow) {
            showCustomWindow(WindowConfigStore.load())
        } else {
            dismissCustomWindow()
        }
    }

    /**
     * 啟動或更新客製化懸浮視窗
     */
    fun showCustomWindow(
        config: WindowCustomConfig,
        onStateChanged: ((WindowCustomConfig) -> Unit)? = null,
        onDismissed: (() -> Unit)? = null
    ): Boolean {
        val activity = MainActivity.mainActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "showCustomWindow failed: Activity is unavailable.")
            return false
        }

        if (onStateChanged != null) windowStateListener = onStateChanged
        if (onDismissed != null) windowDismissListener = onDismissed

        val existing = mediaMenuWindow
        if (existing != null && existing.isShowing && existing.windowMode == config.mode) {
            existing.onConfigUpdatedListener = { updated -> windowStateListener?.invoke(updated) }
            existing.applyConfig(config)
            return true
        }

        if (existing != null) {
            existing.setOnCustomDismissListener(null)
            existing.dismiss()
            mediaMenuWindow = null
        }

        return try {
            val window = MediaMenuWindow(activity, config)
            if (!window.isShowing) {
                window.dismiss()
                return false
            }
            window.onConfigUpdatedListener = { updated ->
                windowStateListener?.invoke(updated)
            }
            window.setOnCustomDismissListener(object : BaseWindow.OnCustomDismissListener {
                override fun onDismiss() {
                    if (mediaMenuWindow == window) {
                        mediaMenuWindow = null
                    }
                    windowDismissListener?.invoke()
                }
            })
            mediaMenuWindow = window
            true
        } catch (e: Exception) {
            Log.e(TAG, "showCustomWindow failed: ${e.message}", e)
            mediaMenuWindow = null
            false
        }
    }

    fun updateCustomWindow(config: WindowCustomConfig) {
        val existing = mediaMenuWindow ?: return
        if (!existing.isShowing) return
        if (existing.windowMode != config.mode) {
            showCustomWindow(config, windowStateListener, windowDismissListener)
        } else {
            existing.applyConfig(config)
        }
    }

    fun setCustomWindowListeners(
        onStateChanged: ((WindowCustomConfig) -> Unit)?,
        onDismissed: (() -> Unit)?
    ) {
        windowStateListener = onStateChanged
        windowDismissListener = onDismissed
        mediaMenuWindow?.onConfigUpdatedListener = { updated ->
            windowStateListener?.invoke(updated)
        }
    }

    fun snapCustomWindowToCorner(corner: DockCorner) {
        mediaMenuWindow?.takeIf { it.isShowing }?.snapToCorner(corner)
    }

    fun resetCustomWindowPosition() {
        mediaMenuWindow?.takeIf { it.isShowing }?.resetToDefaultPosition()
    }

    fun isCustomWindowShowing(): Boolean {
        return mediaMenuWindow?.isShowing == true
    }

    fun dismissCustomWindow() {
        try {
            mediaMenuWindow?.dismiss()
        } catch (e: Exception) {
            Log.e(TAG, "dismiss mediaMenuWindow failed: ${e.message}")
        } finally {
            mediaMenuWindow = null
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
