package com.example.lin_new_project_kotlin_ai.viewBinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.viewbinding.ViewBinding
import com.example.lin_new_project_kotlin_ai.R

abstract class BaseWindow<VB : ViewBinding?>(
    context: Context,
    width: Int,
    height: Int,
    gravity: Int,
    x: Int,
    y: Int
) {
    @get:JvmName("Contextbinding")
    val binding: VB?
    protected val hostContext: Context = context.applicationContext
    protected var mWindowManager: WindowManager?
    protected var layoutParams: WindowManager.LayoutParams?
    var isShowing: Boolean = false
        private set
    private var dismissNotified = false

    fun setVisibleFade(isShow: Boolean) {
        val root = binding?.root ?: return
        root.animate().cancel()
        if (isShow) {
            root.alpha = 0f
            root.visibility = View.VISIBLE
            root.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
        } else {
            root.animate()
                .alpha(0f)
                .setDuration(220)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        root.visibility = View.INVISIBLE
                        root.alpha = 1f
                        root.animate().setListener(null)
                    }
                })
        }
    }

    fun setVisible(isShow: Boolean) {
        binding?.root?.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
    }

    @JvmName("onCustomDismiss")
    fun setOnCustomDismissListener(onCustomDismissListener: OnCustomDismissListener?) {
        this.onCustomDismissListener = onCustomDismissListener
    }

    var onCustomDismissListener: OnCustomDismissListener? = null

    init {
        val themedContext = ContextThemeWrapper(hostContext, R.style.TranslucentAppTheme)
        binding = onCreateViewBinding(LayoutInflater.from(themedContext))

        mWindowManager = hostContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val params = WindowManager.LayoutParams()
        params.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        params.format = PixelFormat.TRANSLUCENT
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        params.flags = (
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )

        params.width = resolveDimension(width)
        params.height = resolveDimension(height)
        params.gravity = gravity
        params.x = x
        params.y = y
        layoutParams = params

        attachToWindow()
    }

    private fun resolveDimension(size: Int): Int {
        return when {
            size == 0 -> ViewGroup.LayoutParams.MATCH_PARENT
            size == 1 -> ViewGroup.LayoutParams.WRAP_CONTENT
            size < 0 -> size
            else -> dp2px(size.toFloat())
        }
    }

    private fun attachToWindow(): Boolean {
        if (isShowing) return true
        val root = binding?.root ?: return false
        val wm = mWindowManager ?: return false
        val params = layoutParams ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(hostContext)) {
            Log.w(TAG, "Cannot attach overlay window without SYSTEM_ALERT_WINDOW permission")
            return false
        }

        return try {
            wm.addView(root, params)
            isShowing = true
            dismissNotified = false
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach window view: ${e.message}", e)
            isShowing = false
            false
        }
    }

    interface OnCustomDismissListener {
        fun onDismiss()
    }

    open fun dismiss() {
        try {
            val root = binding?.root
            root?.animate()?.cancel()
            if (root != null && root.isAttachedToWindow) {
                mWindowManager?.removeViewImmediate(root)
            }
        } catch (e: Exception) {
            Log.e(TAG, "dismiss error: ${e.message}", e)
        } finally {
            isShowing = false
            if (!dismissNotified) {
                dismissNotified = true
                onCustomDismissListener?.onDismiss()
            }
            layoutParams = null
            mWindowManager = null
        }
    }

    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB?

    protected fun fullScreenImmersive(view: View) {
        @Suppress("DEPRECATION")
        val uiOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        @Suppress("DEPRECATION")
        view.systemUiVisibility = uiOptions
    }

    fun getBinding(): VB? {
        return binding
    }

    companion object {
        private const val TAG = "BaseWindow"
    }
}
