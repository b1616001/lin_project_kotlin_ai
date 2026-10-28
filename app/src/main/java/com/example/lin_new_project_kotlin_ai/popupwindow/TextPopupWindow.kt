package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.view.LayoutInflater
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowTextBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class TextPopupWindow(context: Context) : BasePopupWindow<PopupWindowTextBinding?>(
    context,
    350,
    500,
    300,
    General.FADE,
    true,
    true,
    false,
    true
) {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowTextBinding {
        return PopupWindowTextBinding.inflate(layoutInflater)
    }

    init {
        initView()
    }

    private fun initView() {
        binding?.imgClosed?.setOnClickListener { dismiss() }
    }
}
