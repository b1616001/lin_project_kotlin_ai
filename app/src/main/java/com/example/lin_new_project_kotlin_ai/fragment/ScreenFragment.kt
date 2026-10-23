package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.lin_new_project_kotlin_ai.databinding.FragmentScreenBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class ScreenFragment : BaseBindingFragment<FragmentScreenBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentScreenBinding {
        return FragmentScreenBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getBinding()!!.tvOn.setOnClickListener { view: View? ->
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } //不會關屏幕
        getBinding()!!.tvOff.setOnClickListener { view: View? ->
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } //屏幕開關正常
    }
}