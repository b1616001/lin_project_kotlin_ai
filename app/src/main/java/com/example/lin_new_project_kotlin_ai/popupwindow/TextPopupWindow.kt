package com.example.lin_new_project_kotlin_ai.popupwindow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.lin_new_project_kotlin_ai.General
import com.example.lin_new_project_kotlin_ai.databinding.PopupWindowTextBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BasePopupWindow

/**
 * 優化版通用文字彈窗元件 (TextPopupWindow)
 * - 繼承自 BasePopupWindow，支援淡入/淡出平滑動畫、暗色背景聯動與生命週期安全保護
 * - 支援標題、自定義內文、副標籤、一鍵複製至剪貼簿與確認回呼
 * - 具備完整建構子多型，向下相容原有 TextPopupWindow(context) 調用方式
 */
class TextPopupWindow @JvmOverloads constructor(
    context: Context,
    private var titleText: String = "自定義彈窗通知",
    private var messageText: String = "這是一個客製化的 PopupWindow 元件！\n\n" +
            "• 支援平滑硬體加速進出場動畫\n" +
            "• 具備半透明遮罩背景自動聯動\n" +
            "• 支援點擊外部觸控或按鈕自動關閉\n" +
            "• 內建一鍵複製內文與回呼事件處理",
    @General.animationType animateType: Int = General.FADE,
    private var onConfirmAction: (() -> Unit)? = null
) : BasePopupWindow<PopupWindowTextBinding?>(
    context,
    300,
    1,     // WRAP_CONTENT
    320,   // 320dp 寬度
    animateType,
    true,  // isTouchCancel: 點擊外部可關閉
    true,  // isDark: 啟用暗色背景遮罩
    false, // isFloating
    true   // isClearDark: 關閉時清除暗色背景
) {

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): PopupWindowTextBinding {
        return PopupWindowTextBinding.inflate(layoutInflater)
    }

    init {
        initView()
    }

    private fun initView() {
        val b = binding ?: return

        b.tvTitle.text = titleText
        b.tvContent.text = messageText

        // 關閉按鈕
        b.imgClosed.setOnClickListener { dismiss() }
        b.btnCancel.setOnClickListener { dismiss() }

        // 確認按鈕
        b.btnConfirm.setOnClickListener {
            onConfirmAction?.invoke()
            dismiss()
        }

        // 複製文字按鈕
        b.btnCopy.setOnClickListener {
            val textToCopy = b.tvContent.text.toString()
            if (textToCopy.isNotEmpty()) {
                val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Popup Content", textToCopy)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(mContext, "已複製內容到剪貼簿", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setTitle(title: CharSequence): TextPopupWindow {
        this.titleText = title.toString()
        binding?.tvTitle?.text = title
        return this
    }

    fun setContent(content: CharSequence): TextPopupWindow {
        this.messageText = content.toString()
        binding?.tvContent?.text = content
        return this
    }

    fun setTag(tagText: CharSequence): TextPopupWindow {
        binding?.tvTag?.text = tagText
        return this
    }

    fun setConfirmText(confirmText: CharSequence): TextPopupWindow {
        binding?.btnConfirm?.text = confirmText
        return this
    }

    fun setOnConfirmListener(action: () -> Unit): TextPopupWindow {
        this.onConfirmAction = action
        return this
    }
}
