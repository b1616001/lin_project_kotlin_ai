package com.example.lin_new_project_kotlin_ai.window

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.example.lin_new_project_kotlin_ai.databinding.WindowMediaMenuBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import kotlin.math.hypot
import kotlin.math.roundToInt

class MediaMenuWindow(context: Context) : BaseWindow<WindowMediaMenuBinding?>(
    context = context,
    width = 1,
    height = 1,
    gravity = Gravity.CENTER,
    x = 0,
    y = 0
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    init {
        initView()
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): WindowMediaMenuBinding {
        return WindowMediaMenuBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        val b = getBinding() ?: return
        val touchSlop = ViewConfiguration.get(hostContext).scaledTouchSlop.toFloat()
        val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

        b.btnClose.setOnClickListener {
            dismiss()
        }

        b.btnDrag.setOnTouchListener(object : View.OnTouchListener {
            private var downRawX = 0f
            private var downRawY = 0f
            private var lastRawX = 0f
            private var lastRawY = 0f
            private var isDragEnabled = false

            private fun resetDragState(view: View?) {
                longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                longPressRunnable = null
                isDragEnabled = false
                view?.isPressed = false
                b.btnDrag.alpha = 1f
                b.btnDrag.scaleX = 1f
                b.btnDrag.scaleY = 1f
            }

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                val params = layoutParams ?: return false
                val root = getBinding()?.root ?: return false

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downRawX = event.rawX
                        downRawY = event.rawY
                        lastRawX = event.rawX
                        lastRawY = event.rawY
                        isDragEnabled = false
                        v?.isPressed = true

                        longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                        val runnable = Runnable {
                            isDragEnabled = true
                            v?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            b.btnDrag.alpha = 0.82f
                            b.btnDrag.scaleX = 0.95f
                            b.btnDrag.scaleY = 0.95f
                        }
                        longPressRunnable = runnable
                        mainHandler.postDelayed(runnable, longPressTimeout)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!isDragEnabled) {
                            val movedDistance = hypot(event.rawX - downRawX, event.rawY - downRawY)
                            lastRawX = event.rawX
                            lastRawY = event.rawY
                            if (movedDistance > touchSlop) {
                                longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                                longPressRunnable = null
                            }
                            return true
                        }

                        val dx = (event.rawX - lastRawX).roundToInt()
                        val dy = (event.rawY - lastRawY).roundToInt()
                        lastRawX = event.rawX
                        lastRawY = event.rawY
                        params.x += dx
                        params.y += dy
                        if (isShowing && root.isAttachedToWindow) {
                            mWindowManager?.updateViewLayout(root, params)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        val wasDragging = isDragEnabled
                        val movedDistance = hypot(event.rawX - downRawX, event.rawY - downRawY)
                        resetDragState(v)
                        if (!wasDragging && movedDistance <= touchSlop) {
                            v?.performClick()
                        }
                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        resetDragState(v)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dismiss() {
        longPressRunnable?.let { mainHandler.removeCallbacks(it) }
        longPressRunnable = null
        super.dismiss()
    }
}
