package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTtsBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Locale

class TtsFragment : BaseBindingFragment<FragmentTtsBinding?>(), OnInitListener {

    private var mTTS: TextToSpeech? = null
    private var isTtsReady = false

    private var currentRate = 1.0f
    private var currentPitch = 1.0f
    private var selectedLocale = Locale.TRADITIONAL_CHINESE

    private val languages = listOf(
        LanguageItem("繁體中文 (zh-TW)", Locale.TRADITIONAL_CHINESE),
        LanguageItem("英文 (English - US)", Locale.US),
        LanguageItem("日文 (日本語 - JP)", Locale.JAPAN),
        LanguageItem("簡體中文 (zh-CN)", Locale.SIMPLIFIED_CHINESE),
        LanguageItem("西班牙文 (Español)", Locale("es", "ES"))
    )

    data class LanguageItem(val name: String, val locale: Locale) {
        override fun toString(): String = name
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTtsBinding {
        return FragmentTtsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTTS = TextToSpeech(requireContext().applicationContext, this)
        initViews()
    }

    private fun initViews() {
        val b = binding ?: return

        // 預設示範文字
        b.editTts.setText("歡迎使用 Android 語音朗讀合成測試，這是一段流暢自然的語音示範。")

        // 語言選單 Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        b.spinnerLanguage.adapter = adapter
        b.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLocale = languages[position].locale
                applyLanguage(selectedLocale)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 範例按鈕快捷鍵
        b.btnPresetZh.setOnClickListener {
            b.editTts.setText("歡迎使用繁體中文語音朗讀合成測試，音質自然清晰！")
            b.spinnerLanguage.setSelection(0)
        }
        b.btnPresetEn.setOnClickListener {
            b.editTts.setText("Hello! Welcome to the Android Text-to-Speech synthesis demo. Enjoy natural voice reading!")
            b.spinnerLanguage.setSelection(1)
        }
        b.btnPresetJa.setOnClickListener {
            b.editTts.setText("こんにちは！Androidの音声合成テストへようこそ。滑らかな音声をお楽しみください。")
            b.spinnerLanguage.setSelection(2)
        }
        b.btnClearText.setOnClickListener {
            b.editTts.setText("")
        }

        // 語速 SeekBar (0..150 -> 0.5x .. 2.0x)
        b.sbRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentRate = 0.5f + (progress / 100.0f)
                b.tvRateLabel.text = String.format(Locale.US, "語速 (Speech Rate): %.2fx", currentRate)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 音調 SeekBar (0..150 -> 0.5x .. 2.0x)
        b.sbPitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentPitch = 0.5f + (progress / 100.0f)
                val pitchType = when {
                    currentPitch < 0.85f -> "低沉男聲"
                    currentPitch > 1.25f -> "清脆高音"
                    else -> "標準常規"
                }
                b.tvPitchLabel.text = String.format(Locale.US, "音調 (Pitch): %.2fx (%s)", currentPitch, pitchType)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 開始朗讀按鈕
        b.btnTts.setOnClickListener {
            val text = b.editTts.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(context, "請先輸入欲朗讀的文字內容", Toast.LENGTH_SHORT).show()
                speak("請先在輸入框輸入欲朗讀的文字內容")
            } else {
                speak(text)
            }
        }

        // 停止朗讀按鈕
        b.btnStopTts.setOnClickListener {
            stopSpeaking()
        }

        // 重設按鈕
        b.btnResetTts.setOnClickListener {
            resetSettings()
        }
    }

    override fun onInit(status: Int) {
        val b = binding ?: return
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupUtteranceListener()
            applyLanguage(selectedLocale)
        } else {
            isTtsReady = false
            b.tvTtsStatus.text = "引擎狀態: 初始化失敗 ($status)"
            Toast.makeText(context, "TTS 初始化失敗，請確認系統語音引擎設定", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUtteranceListener() {
        mTTS?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                activity?.runOnUiThread {
                    binding?.tvTtsStatus?.text = "引擎狀態: 正在朗讀語音中..."
                }
            }

            override fun onDone(utteranceId: String?) {
                activity?.runOnUiThread {
                    binding?.tvTtsStatus?.text = "引擎狀態: 朗讀完畢 (就緒)"
                }
            }

            override fun onError(utteranceId: String?) {
                activity?.runOnUiThread {
                    binding?.tvTtsStatus?.text = "引擎狀態: 朗讀過程發生錯誤"
                }
            }
        })
    }

    private fun applyLanguage(locale: Locale) {
        val tts = mTTS ?: return
        val availability = tts.isLanguageAvailable(locale)
        val b = binding ?: return

        when (availability) {
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                tts.language = locale
                b.tvTtsStatus.text = "引擎狀態: 語音準備就緒 (${locale.displayLanguage})"
            }
            TextToSpeech.LANG_MISSING_DATA -> {
                b.tvTtsStatus.text = "引擎狀態: 缺少此語言資料 (${locale.displayLanguage})"
                Toast.makeText(context, "系統缺少 ${locale.displayLanguage} 的語音檔", Toast.LENGTH_SHORT).show()
            }
            TextToSpeech.LANG_NOT_SUPPORTED -> {
                b.tvTtsStatus.text = "引擎狀態: 不支援此語言 (${locale.displayLanguage})"
                Toast.makeText(context, "此裝置 TTS 引擎不支援 ${locale.displayLanguage}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                b.tvTtsStatus.text = "引擎狀態: 語言狀態未知 ($availability)"
            }
        }
    }

    private fun speak(text: String) {
        if (!isTtsReady || mTTS == null) {
            Toast.makeText(context, "語音引擎尚未就緒，請稍候", Toast.LENGTH_SHORT).show()
            return
        }

        val tts = mTTS!!
        tts.setPitch(currentPitch)
        tts.setSpeechRate(currentRate)

        val utteranceId = "utterance_${System.currentTimeMillis()}"
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.SUCCESS) {
            binding?.tvTtsStatus?.text = "引擎狀態: 正在播放語音..."
        } else {
            Toast.makeText(context, "語音播放失敗 ($result)", Toast.LENGTH_SHORT).show()
            binding?.tvTtsStatus?.text = "引擎狀態: 播放失敗"
        }
    }

    private fun stopSpeaking() {
        mTTS?.stop()
        binding?.tvTtsStatus?.text = "引擎狀態: 已停止播放 (就緒)"
        Toast.makeText(context, "已停止朗讀", Toast.LENGTH_SHORT).show()
    }

    private fun resetSettings() {
        val b = binding ?: return
        b.sbRate.progress = 50
        b.sbPitch.progress = 50
        currentRate = 1.0f
        currentPitch = 1.0f
        b.tvRateLabel.text = "語速 (Speech Rate): 1.0x"
        b.tvPitchLabel.text = "音調 (Pitch): 1.0x (標準常規)"
        b.spinnerLanguage.setSelection(0)
        stopSpeaking()
        Toast.makeText(context, "已重設語速與音調參數", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        mTTS?.stop()
        mTTS?.shutdown()
        mTTS = null
        isTtsReady = false
        super.onDestroyView()
    }
}