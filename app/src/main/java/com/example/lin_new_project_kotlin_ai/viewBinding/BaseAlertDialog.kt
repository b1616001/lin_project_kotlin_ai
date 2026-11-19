package com.example.lin_new_project_kotlin_ai.viewBinding

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

abstract class BaseAlertDialog<VB : ViewBinding?> protected constructor(
    context: Context?
) : AlertDialog(context) {
    private var _binding: VB? = null

    @get:JvmName("bindingProperty")
    val binding: VB?
        get() = _binding

    init {
        val b = onCreateViewBinding(LayoutInflater.from(context))
        _binding = b
        b?.root?.let { setView(it) }
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB

    fun getBinding(): VB? {
        return _binding
    }
}
