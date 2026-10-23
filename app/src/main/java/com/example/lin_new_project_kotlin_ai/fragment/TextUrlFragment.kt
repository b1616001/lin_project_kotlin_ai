package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTextUrlBinding
import com.example.lin_new_project_kotlin_ai.`fun`.UrlUtilsMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class TextUrlFragment : BaseBindingFragment<FragmentTextUrlBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTextUrlBinding {
        return FragmentTextUrlBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UrlUtilsMethod.handleText(
            getBinding()!!.tvUrl,
            "測試網址:https://github.com支持http、https、svn、ftp"
        )
    }
}