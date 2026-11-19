package com.example.lin_new_project_kotlin_ai.viewBinding

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.viewbinding.ViewBinding

abstract class BaseView<VB : ViewBinding?> : LinearLayout {
    private var _binding: VB? = null

    @get:JvmName("bindingProperty")
    val binding: VB?
        get() = _binding

    constructor(context: Context) : super(context) {
        _binding = onCreateViewBinding(LayoutInflater.from(context))
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val layoutParams = LayoutParams(context, attrs).apply {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        val b = onCreateViewBinding(LayoutInflater.from(context))
        _binding = b
        b?.root?.let { addView(it, layoutParams) }
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB

    fun getBinding(): VB? {
        return _binding
    }

    interface OnCustomDismissListener {
        fun onDismiss()
    }

    fun setLog(mes: String?) {
        Log.d(javaClass.simpleName, mes ?: "")
    }
}
