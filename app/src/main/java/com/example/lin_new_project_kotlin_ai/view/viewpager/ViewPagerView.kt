package com.example.lin_new_project_kotlin_ai.view.viewpager

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.viewbinding.ViewBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod

abstract class ViewPagerView<VB : ViewBinding?>(context: Context) : LinearLayout(context) {
    @JvmField
    val binding: VB?
    private val context: Context?

    init {
        this.context = context
        binding = onCreateViewBinding(LayoutInflater.from(context))
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB?
    abstract fun dismiss()
    abstract fun initData()

    fun showToast(msg: String?) {
        ToastMethod.showToast(context, msg)
    }
}
