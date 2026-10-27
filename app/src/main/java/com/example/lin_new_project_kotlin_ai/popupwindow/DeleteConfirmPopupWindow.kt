package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.os.Looper
import android.os.MessageQueue
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowConfirmDeleteBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class DeleteConfirmPopupWindow(
    context: Context?,
    private val title: String,
    private val message: String,
    private val onConfirm: () -> Unit
) : BasePopupWindow<PopupWindowConfirmDeleteBinding?>(
    context,
    300,
    1,
    320,
    General.FADE,
    false,
    true,
    false,
    true
) {

    init {
        isFocusable = true
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowConfirmDeleteBinding {
        return PopupWindowConfirmDeleteBinding.inflate(layoutInflater)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        Looper.myQueue().addIdleHandler(MessageQueue.IdleHandler {
            initView()
            false
        })
    }

    private fun initView() {
        val binding = getBinding() ?: return
        binding.tvTitle.text = title
        binding.tvMessage.text = message

        binding.imgClosed.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirmDelete.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }
}
