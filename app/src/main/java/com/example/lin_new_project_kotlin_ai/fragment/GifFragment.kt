package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.FragmentGifBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class GifFragment : BaseBindingFragment<FragmentGifBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentGifBinding {
        return FragmentGifBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}