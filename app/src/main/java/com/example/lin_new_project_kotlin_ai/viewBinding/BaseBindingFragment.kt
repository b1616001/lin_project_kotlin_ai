package com.example.lin_new_project_kotlin_ai.viewBinding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod

abstract class BaseBindingFragment<VB : ViewBinding?> : Fragment() {
    private var _binding: VB? = null

    @get:JvmName("bindingProperty")
    val binding: VB?
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = onCreateViewBinding(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun onCreateViewBinding(layoutInflater: LayoutInflater): VB

    fun getBinding(): VB? {
        return _binding
    }

    override fun onResume() {
        mainActivity?.currentFragment = javaClass.simpleName
        super.onResume()
    }

    fun setLog(mes: String?) {
        Log.d(javaClass.simpleName, mes ?: "")
    }

    fun showToast(msg: String?) {
        ToastMethod.showToast(context, msg)
    }
}
