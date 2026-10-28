package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.view.LayoutInflater
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowConfirmDeleteBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class DeleteConfirmPopupWindow(
    context: Context,
    private val title: String,
    private val message: String,
    private val onConfirm: () -> Unit
) : BasePopupWindow<PopupWindowConfirmDeleteBinding?>(
    context,
    300,
    1,
    320,
    General.FADE,
    true,
    true,
    false,
    true
) {

    init {
        initView()
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowConfirmDeleteBinding {
        return PopupWindowConfirmDeleteBinding.inflate(layoutInflater)
    }

    private fun initView() {
        val b = binding ?: return
        b.tvTitle.text = title
        b.tvMessage.text = message

        b.imgClosed.setOnClickListener { dismiss() }
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnConfirmDelete.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }
}
