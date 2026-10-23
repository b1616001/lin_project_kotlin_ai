package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTtsBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale


class TtsFragment : BaseBindingFragment<FragmentTtsBinding?>(), OnInitListener {
    private var mTTS: TextToSpeech? = null
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTtsBinding {
        return FragmentTtsBinding.inflate(layoutInflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTTS = TextToSpeech(getContext(), this)
        initView()
    }

    private fun initView() {
        getBinding()!!.btnTts.setOnClickListener { view: View? ->
            if (CommonUtils.checkStr(getBinding()!!.editTts.getText().toString())) {
                speak(getBinding()!!.editTts.getText().toString().trim { it <= ' ' })
            } else {
                speak("輸入框空白   哈哈哈 ")
            }
        }
    }

    override fun onInit(i: Int) {
        val locale = Locale.TRADITIONAL_CHINESE
        if (mTTS!!.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE ||
            mTTS!!.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            mTTS!!.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        ) {
            setLog("初始化" + mTTS!!.isLanguageAvailable(locale) + "成功")
            mTTS!!.setLanguage(locale)
        } else {
            setLog("初始化" + mTTS!!.isLanguageAvailable(locale) + "失敗")
        }
    }

    fun speak(text: String?): Boolean {
        setLog("speak text:$text" )
        // 設定音調，值越大聲音越尖（女生），值越小則變成男聲,1.0是常規
        mTTS!!.setPitch(1.0f)
        // 設定語速
        mTTS!!.setSpeechRate(1.0f)
        //播放語音
        val ret = mTTS!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        if (ret == 0) {
            Toast.makeText(getActivity(), "文字轉語音成功...", Toast.LENGTH_SHORT).show()
            return true
        } else {
            Toast.makeText(getActivity(), "文字轉語音錯誤...", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        /* 釋放TextToSpeech的資源 */
        mTTS!!.shutdown()

        super.onDestroy()
    }
}