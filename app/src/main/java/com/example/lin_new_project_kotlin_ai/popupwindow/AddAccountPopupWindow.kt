package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowAddAccountBinding
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class AddAccountPopupWindow(
    context: Context,
    private val onConfirm: (UserData) -> Unit
) : BasePopupWindow<PopupWindowAddAccountBinding?>(
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

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowAddAccountBinding {
        return PopupWindowAddAccountBinding.inflate(layoutInflater)
    }

    private fun initView() {
        val b = binding ?: return
        val randomNum = (100..999).random()
        b.etUserName.setText("User_$randomNum")
        b.etImageName.setText("img_$randomNum")
        b.rbUser.isChecked = true

        b.imgClosed.setOnClickListener { dismiss() }
        b.btnCancel.setOnClickListener { dismiss() }

        b.btnConfirm.setOnClickListener {
            val name = b.etUserName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(mContext, "請輸入使用者名稱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userType = if (b.rbUser.isChecked) 1 else 0
            val imageName = b.etImageName.text.toString().trim().ifEmpty { "img_$randomNum" }

            val userData = UserData().apply {
                this.userName = name
                this.userType = userType
                this.imageName = imageName
            }

            onConfirm(userData)
            dismiss()
        }
    }
}
