package com.example.lin_new_project_kotlin_ai.view.viewpager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.ViewRedBinding

class RedView(context: Context) : ViewPagerView<ViewRedBinding?>(context) {
    private val context: Context?
    public override fun onCreateViewBinding(layoutInflater: LayoutInflater): ViewRedBinding? {
        return ViewRedBinding.inflate(layoutInflater)
    }

    public override fun dismiss() {
    }

    public override fun initData() {
    }

    init {
        this.context = context
        binding!!.imageView.setOnClickListener { view: View? -> showToast("紅色") }
    }
}
