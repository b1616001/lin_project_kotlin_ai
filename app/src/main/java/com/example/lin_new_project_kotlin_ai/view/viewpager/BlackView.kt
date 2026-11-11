package com.example.lin_new_project_kotlin_ai.view.viewpager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.ViewBlackBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlackView(context: Context) : ViewPagerView<ViewBlackBinding?>(context) {
    private var initCount = 0
    private var dismissCount = 0
    private var clickCount = 0
    var onNextAction: (() -> Unit)? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): ViewBlackBinding? {
        return ViewBlackBinding.inflate(layoutInflater)
    }

    override fun initData() {
        initCount++
        binding?.let { b ->
            b.tvStatus.text = "活躍中 (Active)"
            b.tvStatus.setBackgroundResource(R.drawable.bg_badge_active)
            b.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            b.tvInitCount.text = "initData() 觸發: ${initCount} 次"
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            b.tvLastActive.text = "最後喚醒時間: $time"
        }
    }

    override fun dismiss() {
        dismissCount++
        binding?.let { b ->
            b.tvStatus.text = "待命中 (Idle)"
            b.tvStatus.setBackgroundResource(R.drawable.bg_badge_idle)
            b.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            b.tvDismissCount.text = "dismiss() 觸發: ${dismissCount} 次"
        }
    }

    init {
        binding?.let { b ->
            b.imageView.setOnClickListener {
                showToast("黑色 (Obsidian Dark) #212121")
            }
            b.btnClickCount.setOnClickListener {
                clickCount++
                b.tvClickCount.text = "累計點擊: ${clickCount} 次"
                showToast("黑色互動計數 +1")
            }
            b.btnCopyHex.setOnClickListener {
                copyToClipboard("#212121")
                showToast("已複製色碼 #212121 到剪貼簿")
            }
            b.btnNext.setOnClickListener {
                onNextAction?.invoke()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Color Code", text)
        clipboard?.setPrimaryClip(clip)
    }
}
