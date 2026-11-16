package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentLanguageBinding
import com.example.lin_new_project_kotlin_ai.enums.LanguageEnum
import com.example.lin_new_project_kotlin_ai.`fun`.LanguageUtils
import com.example.lin_new_project_kotlin_ai.`fun`.MmkvUtils
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment

class LanguageFragment : BaseBindingFragment<FragmentLanguageBinding?>() {
    private var languageEnum: LanguageEnum? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        languageEnum = LanguageEnum.getLanguageEnum(MmkvUtils.getLanguageCountry(), false)
        initView()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCn.setOnClickListener {
            setLanguage(LanguageEnum.Chinese)
        }

        binding.btnTw.setOnClickListener {
            setLanguage(LanguageEnum.TW)
        }

        updatePreviewCard()
    }

    private fun updatePreviewCard() {
        val binding = getBinding() ?: return
        val current = languageEnum ?: LanguageEnum.TW
        val localeTag = "${current.languageCode}-${current.country}"
        val label = when (current) {
            LanguageEnum.TW -> "繁體中文"
            LanguageEnum.Chinese -> "简体中文"
            else -> current.language
        }
        binding.tvCurrentLanguage.text = "${getString(R.string.language_current_prefix)}$label ($localeTag)"
    }

    fun setLanguage(language: LanguageEnum) {
        languageEnum = language
        updatePreviewCard()
        LanguageUtils.refreshApp(mainActivity!!, languageEnum)
    }
}