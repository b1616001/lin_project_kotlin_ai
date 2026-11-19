package com.example.lin_new_project_kotlin_ai.viewBinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseBindingActivity<VB : ViewBinding?> : AppCompatActivity() {
    private var _binding: VB? = null

    @get:JvmName("bindingProperty")
    val binding: VB?
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = onCreateViewBinding(layoutInflater)
        _binding = b
        b?.root?.let { setContentView(it) }
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB

    fun getBinding(): VB? {
        return _binding
    }

    override fun onResume() {
        super.onResume()
    }

    @Suppress("DEPRECATION")
    fun fullScreenImmersive(view: View?) {
        val target = view ?: return
        val uiOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        target.systemUiVisibility = uiOptions
    }
}
