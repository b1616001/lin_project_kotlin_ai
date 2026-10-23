package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentQrcodeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CodeUtils
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils.Companion.checkStr
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class QrcodeFragment : BaseBindingFragment<FragmentQrcodeBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentQrcodeBinding {
        return FragmentQrcodeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBinding()!!.tvQrcode.setOnClickListener { view1: View? ->
            if (checkStr(getBinding()!!.edQrcode.getText().toString())) {
                val logo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_red)
                val bitmap = CodeUtils.createQRCode(getBinding()!!.edQrcode.getText().toString(), 600, logo)
                getBinding()!!.image.setImageBitmap(bitmap)
            }
        }
    }
}