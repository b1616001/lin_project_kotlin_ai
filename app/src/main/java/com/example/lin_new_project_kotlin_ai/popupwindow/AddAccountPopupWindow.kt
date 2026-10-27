package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.Context
import android.os.Looper
import android.os.MessageQueue
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowAddAccountBinding
import com.example.lin_new_project_kotlin_ai.room.data.UserData
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

class AddAccountPopupWindow(
    context: Context?,
    private val onConfirm: (UserData) -> Unit
) : BasePopupWindow<PopupWindowAddAccountBinding?>(
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

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowAddAccountBinding {
        return PopupWindowAddAccountBinding.inflate(layoutInflater)
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
        val randomNum = (100..999).random()
        binding.etUserName.setText("User_$randomNum")
        binding.etImageName.setText("img_$randomNum")
        binding.rbUser.isChecked = true

        binding.imgClosed.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnConfirm.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(mContext, "請輸入使用者名稱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userType = if (binding.rbUser.isChecked) 1 else 0
            val imageName = binding.etImageName.text.toString().trim().ifEmpty { "img_$randomNum" }

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
