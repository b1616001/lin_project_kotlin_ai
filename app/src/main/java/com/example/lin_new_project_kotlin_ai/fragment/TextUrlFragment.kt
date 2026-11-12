package com.example.lin_new_project_kotlin_ai.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lin_new_project_kotlin_ai.adapter.ExtractedLinkAdapter
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTextUrlBinding
import com.example.lin_new_project_kotlin_ai.`fun`.UrlUtilsMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment

class TextUrlFragment : BaseBindingFragment<FragmentTextUrlBinding?>() {

    private lateinit var linkAdapter: ExtractedLinkAdapter
    private var isCurrentHtml = false

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTextUrlBinding {
        return FragmentTextUrlBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()

        // 預設示範文字 (維持原專案基礎內容並擴充完整度)
        val defaultText = "測試網址:https://github.com 支持 http、https、svn、ftp\n官方首頁: https://www.google.com\n技術支援信箱: service@example.com\n客服專線: 0912-345-678"
        getBinding()?.edInputText?.setText(defaultText)
        parseAndRender(defaultText, isHtml = false)
    }

    private fun setupRecyclerView() {
        val binding = getBinding() ?: return
        linkAdapter = ExtractedLinkAdapter()

        linkAdapter.setOnOpenClickListener { link ->
            UrlUtilsMethod.openLinkSafely(requireContext(), link.targetUrl, link.type)
        }

        linkAdapter.setOnCopyClickListener { link ->
            copyToClipboard(link.targetUrl, "連結")
        }

        binding.rvLinks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = linkAdapter
        }
    }

    private fun setupListeners() {
        val binding = getBinding() ?: return

        binding.btnParse.setOnClickListener {
            val text = binding.edInputText.text.toString()
            if (text.isBlank()) {
                showToast("請先輸入文字內容")
                return@setOnClickListener
            }
            parseAndRender(text, isHtml = isCurrentHtml)
            showToast("解析完成")
        }

        binding.btnClear.setOnClickListener {
            binding.edInputText.setText("")
            binding.tvUrl.text = ""
            linkAdapter.updateList(emptyList())
            binding.tvLinksTitle.text = "🔗 已偵測連結清單 (0)"
            binding.tvEmptyLinks.visibility = View.VISIBLE
            binding.rvLinks.visibility = View.GONE
            showToast("已清空")
        }

        binding.btnCopyAll.setOnClickListener {
            val fullText = binding.tvUrl.text.toString()
            if (fullText.isNotBlank()) {
                copyToClipboard(fullText, "全文內容")
            } else {
                showToast("無內容可複製")
            }
        }

        // 範本切換
        binding.chipMultiUrl.setOnClickListener {
            isCurrentHtml = false
            val text = "【多網址範本】\n" +
                    "GitHub 專案: https://github.com/b1616001/lin_project_kotlin_ai\n" +
                    "Android 官方開發手冊: https://developer.android.com/guide?hl=zh-tw\n" +
                    "快速搜尋入口: www.google.com\n" +
                    "檔案傳輸位址: ftp://speedtest.tele2.net/1MB.zip"
            binding.edInputText.setText(text)
            parseAndRender(text, isHtml = false)
        }

        binding.chipContact.setOnClickListener {
            isCurrentHtml = false
            val text = "【聯絡資訊範本】\n" +
                    "技術諮詢客服: contact@example.com\n" +
                    "商務合作信箱: business.partner@domain.org\n" +
                    "台灣免付費專線: 0800-092-000\n" +
                    "行動客服電話: 0912-345-678\n" +
                    "點擊上方任一信箱或電話即可立即撥號或發送郵件！"
            binding.edInputText.setText(text)
            parseAndRender(text, isHtml = false)
        }

        binding.chipHtml.setOnClickListener {
            isCurrentHtml = true
            val text = "<b>【HTML 超連結標籤範本】</b><br>" +
                    "歡迎參考 <a href='https://kotlinlang.org'>Kotlin 官方網站</a> 與 " +
                    "<a href='https://developer.android.com/jetpack/compose'>Jetpack Compose 指南</a>，" +
                    "亦可來信 <a href='mailto:developer@example.com'>developer@example.com</a> 洽詢。"
            binding.edInputText.setText(text)
            parseAndRender(text, isHtml = true)
        }
    }

    private fun parseAndRender(content: String, isHtml: Boolean) {
        val binding = getBinding() ?: return

        if (isHtml) {
            UrlUtilsMethod.handleHtmlText(
                binding.tvUrl,
                content
            ) { url, type ->
                showLinkActionDialog(url, type)
            }
        } else {
            UrlUtilsMethod.handleText(
                binding.tvUrl,
                content
            ) { url, type ->
                showLinkActionDialog(url, type)
            }
        }

        // 同步萃取連結清單供列表展示
        val links = UrlUtilsMethod.extractLinks(content)
        binding.tvLinksTitle.text = "🔗 已偵測連結清單 (${links.size})"
        if (links.isEmpty()) {
            binding.tvEmptyLinks.visibility = View.VISIBLE
            binding.rvLinks.visibility = View.GONE
        } else {
            binding.tvEmptyLinks.visibility = View.GONE
            binding.rvLinks.visibility = View.VISIBLE
            linkAdapter.updateList(links)
        }
    }

    private fun showLinkActionDialog(url: String, type: UrlUtilsMethod.LinkType) {
        val actionTitle = when (type) {
            UrlUtilsMethod.LinkType.EMAIL -> "發送郵件"
            UrlUtilsMethod.LinkType.PHONE -> "撥打電話"
            else -> "在瀏覽器開啟"
        }

        val options = arrayOf(actionTitle, "複製此連結", "分享連結")
        AlertDialog.Builder(requireContext())
            .setTitle("連結操作: $url")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> UrlUtilsMethod.openLinkSafely(requireContext(), url, type)
                    1 -> copyToClipboard(url, "連結")
                    2 -> shareLink(url)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showToast("已複製$label")
    }

    private fun shareLink(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                this.type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            startActivity(Intent.createChooser(shareIntent, "分享連結"))
        } catch (e: Exception) {
            showToast("無法分享連結")
        }
    }
}