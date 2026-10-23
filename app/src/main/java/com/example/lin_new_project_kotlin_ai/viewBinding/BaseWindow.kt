package com.example.lin_new_project_kotlin_ai.viewBinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.PixelFormat
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
    private val mContext: Context
    protected var mWindowManager: WindowManager?
    protected var layoutParams: WindowManager.LayoutParams?

    //    public void setOverlay(boolean isOverlay) {
    //        mWindowManager.removeViewImmediate(binding.getRoot());
    //        if (isOverlay) {
    //            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    //        } else {
    //            layoutParams.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
    //        }
    //        mWindowManager.addView(binding.getRoot(), layoutParams);
    //    }
    //    public void setOverlay(boolean isOverlay) {
    //        mWindowManager.removeViewImmediate(binding.getRoot());
    //        if (isOverlay) {
    //            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    //        } else {
    //            layoutParams.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
    //        }
    //        mWindowManager.addView(binding.getRoot(), layoutParams);
    //    }
    fun setVisibleFade(isShow: Boolean) {
        if (isShow) {
            binding!!.getRoot().setAlpha(0f)
            binding.getRoot().setVisibility(View.VISIBLE)
            binding.getRoot().animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null)
        } else {
            binding!!.getRoot().animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.getRoot().setVisibility(View.INVISIBLE)
                        binding.getRoot().setAlpha(1f)
                        binding.getRoot().animate().setListener(null)
                    }
                })
        }
    }


    fun setVisible(isShow: Boolean) {
        binding!!.getRoot().setVisibility(if (isShow) View.VISIBLE else View.INVISIBLE)
    }

    @JvmName("onCustomDismiss")
    fun setOnCustomDismissListener(onCustomDismissListener: OnCustomDismissListener?) {
        this.onCustomDismissListener = onCustomDismissListener
    }

    public var onCustomDismissListener: OnCustomDismissListener? = null

    init {
        mContext = context.getApplicationContext()


        //        // 获取WindowManager
//        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        //  mView = setUpView(context);
//        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        // 类型
//        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//
//        // 设置flag
//        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
//        params.flags = flags;
//        // 不设置这个弹出框的透明遮罩显示为黑色
//        params.format = PixelFormat.TRANSLUCENT;
//        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
//        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
//        // 不设置这个flag的话，home页的划屏会有问题
        val ctx = ContextThemeWrapper(mContext, R.style.TranslucentAppTheme)
        //  binding = ViewBindingUtil.inflateWithGeneric(this, LayoutInflater.from(ctx));
        binding = onCreateViewBinding(LayoutInflater.from(ctx))

        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

        layoutParams = WindowManager.LayoutParams()
        layoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        layoutParams!!.format = PixelFormat.TRANSLUCENT //讓背景透明
        layoutParams!!.screenOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //SCREEN_ORIENTATION_LANDSCAPE橫屏，SCREEN_ORIENTATION_PORTRAIT直屏

        layoutParams!!.flags =
            (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        //  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        fullScreenImmersive(binding!!.getRoot())

        val w: Int
        val h: Int
        //
//        if (width == 1) w = WRAP_CONTENT;
//        if (width == 0) w = MATCH_PARENT;
//
        if (width == 0) {
            w = ViewGroup.LayoutParams.MATCH_PARENT
        } else if (width == 1) {
            w = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            w = width
        }
        if (height == 0) {
            h = ViewGroup.LayoutParams.MATCH_PARENT
        } else if (width == 1) {
            h = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            h = height
        }
        layoutParams!!.width = w
        // layoutParams.width = width;
        layoutParams!!.height = h
        layoutParams!!.gravity = gravity
        layoutParams!!.x = x
        layoutParams!!.y = y


        //   if (orientation == 0) {
        //   ObjectAnimator.ofFloat(binding.getRoot(), "translationX", layoutParams.width, 0).setDuration(500).start();
//        } else {
//            ObjectAnimator.ofFloat(binding.getRoot(), "translationY", layoutParams.height, 0).setDuration(duration).start();
//        }
        mWindowManager!!.addView(binding!!.getRoot(), layoutParams)
    }

    interface OnCustomDismissListener {
        fun onDismiss()
    }

    open fun dismiss() {
        try {
            if (onCustomDismissListener != null) {
                onCustomDismissListener!!.onDismiss()
            }
            if (binding != null) {
                mWindowManager!!.removeViewImmediate(binding!!.getRoot())
            }
//            binding = null
            layoutParams = null
            mWindowManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().getDisplayMetrics().density
        return (dpValue * scale + 0.5f).toInt()
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB?

    protected fun fullScreenImmersive(view: View) {
        val uiOptions =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        view.setSystemUiVisibility(uiOptions)
    }
    fun getBinding(): VB? {
        return binding
    }

}
