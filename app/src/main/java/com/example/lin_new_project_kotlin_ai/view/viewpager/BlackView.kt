package com.example.lin_new_project_kotlin_ai.view.viewpager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.ViewBlackBinding

class BlackView(context: Context) : ViewPagerView<ViewBlackBinding?>(context) {
    private val context: Context?
    public override fun onCreateViewBinding(layoutInflater: LayoutInflater): ViewBlackBinding? {
        return ViewBlackBinding.inflate(layoutInflater)
    }

    public override fun dismiss() {
    }

    public override fun initData() {
    }

    init {
        this.context = context
        binding!!.imageView.setOnClickListener { view: View? -> showToast("黑色") }
    }
}
