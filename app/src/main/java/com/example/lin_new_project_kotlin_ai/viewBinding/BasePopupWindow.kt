package com.example.lin_new_project_kotlin_ai.viewBinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding
import com.example.lin_new_project_kotlin_ai.General

/**
 * 優化版 BasePopupWindow
 * - 支援 ViewBinding 自動綁定
 * - 平滑淡入/平移動畫 (使用硬體加速層及標準插值器)
 * - 解決原版在 dismiss() 時立即將 binding 置空導致的動畫期間 NullPointerException 競態問題
 * - 解決 duration 遞減導致重複彈窗動畫速度異常的缺陷
 * - 內建生命週期與 Activity 存活狀態檢查，避免 WindowManager BadTokenException
 * - 支援軟鍵盤自動調適 (SOFT_INPUT_ADJUST_RESIZE) 與焦點獲取
 */
abstract class BasePopupWindow<VB : ViewBinding?> : PopupWindow {
    private var _binding: VB? = null

    @get:JvmName("bindingProperty")
    @set:JvmName("setBindingProperty")
    protected var binding: VB?
        get() = _binding
        set(value) {
            _binding = value
        }

    protected var orientation: Int = 0
    protected var parentView: View? = null
    protected var duration: Int = 0
    protected var mContext: Context
    protected var isDark: Boolean = false

    @JvmField
    var isClearDark: Boolean = false

    protected var viewX: Int = 0
    protected var viewY: Int = 0
    private var isDismissing = false

    @get:JvmName("getOnCustomDismissListenerProp")
    @set:JvmName("setOnCustomDismissListenerProp")
    protected var onCustomDismissListener: OnCustomDismissListener? = null

    interface OnCustomDismissListener {
        fun onStartDismiss()
        fun onDismiss()
    }

    /**
     * 簡易建構子：預設 WRAP_CONTENT, 300ms, FADE 動畫, 點擊外部可關閉, 啟用暗色背景
     */
    constructor(context: Context) : this(context, 300, 1, 1, General.FADE, true, true, false, false)

    /**
     * 自訂寬高的建構子
     */
    constructor(context: Context, width: Int, height: Int) : this(
        context,
        300,
        height,
        width,
        General.FADE,
        true,
        true,
        false,
        false
    )

    /**
     * 完整建構子 (完全相容既有調用方式)
     */
    constructor(
        context: Context,
        duration: Int,
        height: Int,
        width: Int,
        @General.animationType animateType: Int,
        isTouchCancel: Boolean,
        isDark: Boolean,
        isFloating: Boolean,
        isClearDark: Boolean
    ) : super(context) {
        this.mContext = context
        this.duration = maxOf(0, duration)
        this.orientation = animateType
        this.isDark = isDark
        this.isClearDark = isClearDark

        val b = onCreateViewBinding(LayoutInflater.from(context))
        _binding = b
        b?.root?.let { setContentView(it) }

        val w = when (width) {
            0 -> MATCH_PARENT
            1 -> WRAP_CONTENT
            else -> dp2px(width.toFloat())
        }

        val h = when (height) {
            0 -> MATCH_PARENT
            1 -> WRAP_CONTENT
            else -> dp2px(height.toFloat())
        }

        setWidth(w)
        setHeight(h)

        // 預設可獲取焦點與外部觸控
        isFocusable = true
        isOutsideTouchable = isTouchCancel
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        @Suppress("DEPRECATION")
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

        if (isFloating) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        fullScreenImmersive(contentView)
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB

    fun getBinding(): VB? {
        return _binding
    }

    override fun showAsDropDown(anchor: View?, xOff: Int, yOff: Int, gravity: Int) {
        if (isHostActivityDead()) return
        super.showAsDropDown(anchor, xOff, yOff, gravity)
        animateIn()
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        if (isHostActivityDead()) return
        super.showAtLocation(parent, gravity, x, y)
        this.parentView = parent
        this.viewX = x
        this.viewY = y
        animateIn()
    }

    private fun isHostActivityDead(): Boolean {
        val act = mContext as? Activity ?: return false
        return act.isFinishing || act.isDestroyed
    }

    protected open fun animateIn() {
        val content = contentView
        if (content == null || duration <= 0 || orientation == General.NONE) return

        content.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val anim = when (orientation) {
            General.TRANSLATION_X -> ObjectAnimator.ofFloat(
                content,
                "translationX",
                if (width > 0) width.toFloat() else 500f,
                0f
            )

            General.TRANSLATION_Y -> ObjectAnimator.ofFloat(
                content,
                "translationY",
                if (height > 0) height.toFloat() else 500f,
                0f
            )

            General.SCALE_X -> ObjectAnimator.ofFloat(content, "scaleX", 0.6f, 1f)
            General.SCALE_Y -> ObjectAnimator.ofFloat(content, "scaleY", 0.6f, 1f)
            General.FADE -> ObjectAnimator.ofFloat(content, "alpha", 0f, 1f)
            else -> ObjectAnimator.ofFloat(content, "alpha", 0f, 1f)
        }
        anim.duration = duration.toLong()
        anim.interpolator = DecelerateInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                content.setLayerType(View.LAYER_TYPE_NONE, null)
            }
        })
        anim.start()
    }

    /**
     * 直接關閉 PopupWindow，無退出動畫
     */
    fun superDismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isDismissing = false
            _binding = null
            parentView = null
            onCustomDismissListener?.onDismiss()
        }
    }

    override fun dismiss() {
        if (isDismissing) return
        isDismissing = true

        onCustomDismissListener?.onStartDismiss()

        if (duration <= 0 || orientation == General.NONE) {
            superDismiss()
            return
        }

        try {
            animateOut { superDismiss() }
        } catch (e: Exception) {
            e.printStackTrace()
            superDismiss()
        }
    }

    private fun animateOut(onComplete: () -> Unit) {
        val content = contentView
        if (content == null) {
            onComplete()
            return
        }

        val exitDuration = maxOf(100, duration - 60).toLong()
        content.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val internalListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                content.setLayerType(View.LAYER_TYPE_NONE, null)
                content.animate().setListener(null)
                onComplete()
            }
        }

        val viewPropertyAnimator = when (orientation) {
            General.TRANSLATION_X -> content.animate()
                .translationX(if (width > 0) width.toFloat() else 500f)

            General.TRANSLATION_Y -> content.animate()
                .translationY(if (height > 0) height.toFloat() else 500f)

            General.SCALE_X -> content.animate().scaleX(0.1f)
            General.SCALE_Y -> content.animate().scaleY(0.1f)
            General.FADE -> content.animate().alpha(0f)
            else -> content.animate().alpha(0f)
        }

        viewPropertyAnimator
            .setInterpolator(AccelerateInterpolator())
            .setDuration(exitDuration)
            .setListener(internalListener)
            .start()
    }

    fun setOnCustomDismissListener(onCustomDismissListener: OnCustomDismissListener?) {
        this.onCustomDismissListener = onCustomDismissListener
    }

    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    @Suppress("DEPRECATION")
    protected fun fullScreenImmersive(view: View?) {
        if (view == null) return
        val uiOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        view.systemUiVisibility = uiOptions
    }
}
