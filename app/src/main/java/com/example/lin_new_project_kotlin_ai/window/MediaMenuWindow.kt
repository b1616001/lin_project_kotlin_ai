package com.example.lin_new_project_kotlin_ai.window

import android.content.Context
import android.graphics.PointF
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.example.lin_new_project_kotlin_ai.databinding.WindowMediaMenuBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseWindow
import kotlin.math.abs

class MediaMenuWindow(context: Context) :
    BaseWindow<WindowMediaMenuBinding?>(context, 500, 1432, Gravity.END or Gravity.TOP, 50, 150) {
    private val viewHeight = 716 //490
    private val maxWidth = 450
    private val minWidth = 160
    private var viewX = 50
    private var viewY = 120
    private val pointF = PointF()
    private var isActionUp = true
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): WindowMediaMenuBinding {
        return WindowMediaMenuBinding.inflate(layoutInflater)
    }

    override fun dismiss() {
        super.dismiss()
    }

    private fun iniView() {
        getBinding()!!.tvMeum.setOnTouchListener(touchListener)
        getBinding()!!.tvDismiss.setOnClickListener { v: View? -> dismiss() }
    }

    var touchListener: OnTouchListener = object : OnTouchListener {
        private var x = 0
        private var y = 0

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.getRawX().toInt()
                    y = event.getRawY().toInt()
                    isActionUp = true
                    pointF.set(event.getX(), event.getY())
                    view.setPressed(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.getRawX().toInt()
                    val nowY = event.getRawY().toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    viewX = viewX - movedX
                    viewY = viewY + movedY
                    layoutParams!!.x = viewX
                    layoutParams!!.y = viewY
                    mWindowManager!!.updateViewLayout(getBinding()!!.getRoot(), layoutParams)
                    //                    update(viewX, viewY, -1, -1, true);
                    if (isRelMove(pointF, event)) {
                        isActionUp = false
                        view.setPressed(false)
                    } else {
                        isActionUp = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isActionUp) {
                        view.performClick()
                    }
                    view.setPressed(false)
                }
            }
            return true // false 會觸發 click, true 不會
        }
    }


    init {
        Looper.myQueue().addIdleHandler(IdleHandler {
            iniView()
            false
        })
    }

    /**
     * 判斷是否真的有移動
     */
    fun isRelMove(downPointF: PointF, moveEvent: MotionEvent): Boolean {
        return moveEvent.getAction() == MotionEvent.ACTION_MOVE && abs(moveEvent.getX() - downPointF.x) > 0
    }
}
