package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.view.LayoutInflater
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.PopuWindowBackgroundBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils.Companion.getColorByVERSION
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class BackgroundWindow(context: Context, isFloating: Boolean, isBackground: Boolean) :
    BasePopupWindow<PopuWindowBackgroundBinding?>(
        context,
        200,
        0,
        0,
        General.FADE,
        false,
        false,
        isFloating,
        false
    ) {

    init {
        val b = binding
        if (b != null) {
            if (isBackground) {
                b.bg.alpha = 0.65f
                b.bg.setBackgroundColor(getColorByVERSION(R.color.black))
            } else {
                b.bg.alpha = 1f
                b.bg.setBackgroundColor(getColorByVERSION(R.color.white))
            }
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopuWindowBackgroundBinding {
        return PopuWindowBackgroundBinding.inflate(layoutInflater)
    }
}
