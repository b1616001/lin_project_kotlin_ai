package com.example.lin_new_project_kotlin_ai.alertDialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.DialogTextBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseAlertDialog

class TextAlertDialog(context: Context?) : BaseAlertDialog<DialogTextBinding?>(context) {
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): DialogTextBinding {
        return DialogTextBinding.inflate(layoutInflater)
    }

    init {
        setCancelable(false)
        initView()
    }

    private fun initView() {
        getBinding()!!.tvYes.setOnClickListener { view: View? ->
            if (onClickListener != null) {
                onClickListener!!.OnClick()
            }
            dismiss()
        }
    }

    fun Show(title: String?, yes: String?) {
        getBinding()!!.tvTitle.setText(title)
        getBinding()!!.tvYes.setText(yes)
        show()
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun OnClick()
    }
}
