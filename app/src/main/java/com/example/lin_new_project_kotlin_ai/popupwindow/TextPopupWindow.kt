package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowTextBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class TextPopupWindow(context: Context?) : BasePopupWindow<PopupWindowTextBinding?>(
    context,
    500,
    500,
    300,
    General.FADE,
    false,
    true,
    false,
    true
) {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowTextBinding {
        return PopupWindowTextBinding.inflate(layoutInflater)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        Looper.myQueue().addIdleHandler(IdleHandler {
            initView()
            false
        })
    }

    private fun initView() {
        getBinding()!!.imgClosed.setOnClickListener { view: View? -> dismiss() }
    }

    override fun dismiss() {
        super.dismiss()
    }
}
