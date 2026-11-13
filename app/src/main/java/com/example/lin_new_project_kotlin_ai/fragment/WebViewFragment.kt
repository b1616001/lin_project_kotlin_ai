package com.example.lin_new_project_kotlin_ai.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.getSystemService
import androidx.navigation.fragment.findNavController
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentWebviewBinding
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.example.lin_new_project_kotlin_ai.webview.MyWebViewClient
import com.example.lin_new_project_kotlin_ai.webview.MyWebViewClient.OnWebViewClient
import com.example.lin_new_project_kotlin_ai.webview.WebSettingsUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebViewFragment : BaseBindingFragment<FragmentWebviewBinding?>(), OnWebViewClient {

    companion object {
        private const val URL_TEXT2 = "file:///android_asset/text2.html"
        private const val URL_TEXT1 = "file:///android_asset/text1.html"
        private const val URL_QA = "file:///android_asset/qa/qa.html"
        private const val URL_AAA = "file:///android_asset/aaa.html"
        private const val URL_GOOGLE = "https://www.google.com"
    }

    var myWebViewClient: MyWebViewClient? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPageLoading = false
    private var isJsPanelVisible = true
    private var hasReceivedMainFrameError = false
    private val logLines = ArrayDeque<String>(6)

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val webView = getBinding()?.webView
            if (webView != null && webView.canGoBack()) {
                webView.goBack()
            } else {
                isEnabled = false
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentWebviewBinding {
        return FragmentWebviewBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        initHeaderAndToolbar()
        initSourcePresets()
        initJsBridgeControls()
        configureWebView()
        loadUrlSafely(URL_TEXT2)
    }

    override fun onResume() {
        super.onResume()
        getBinding()?.webView?.onResume()
    }

    override fun onPause() {
        getBinding()?.webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mainHandler.removeCallbacksAndMessages(null)
        getBinding()?.webView?.let { wv ->
            try {
                wv.stopLoading()
                wv.removeJavascriptInterface("AndroidApp")
                wv.webChromeClient = null
                wv.loadUrl("about:blank")
                wv.clearHistory()
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.destroy()
            } catch (_: Exception) {
                // Ignore WebView teardown exceptions
            }
        }
        myWebViewClient = null
        super.onDestroyView()
    }

    private fun initHeaderAndToolbar() {
        val binding = getBinding() ?: return

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnToggleJsPanel.setOnClickListener {
            isJsPanelVisible = !isJsPanelVisible
            binding.cardJsBridge.visibility = if (isJsPanelVisible) View.VISIBLE else View.GONE
            binding.btnToggleJsPanel.alpha = if (isJsPanelVisible) 1.0f else 0.55f
        }

        binding.etUrl.setOnEditorActionListener { _, actionId, event ->
            val isGo = actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isGo) {
                loadUrlSafely(binding.etUrl.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.btnReloadOrStop.setOnClickListener {
            val wv = binding.webView
            if (isPageLoading) {
                wv.stopLoading()
                isPageLoading = false
                updateLoadingUi(false, 0)
            } else {
                val inputUrl = binding.etUrl.text.toString().trim()
                if (inputUrl.isNotEmpty() && inputUrl != wv.url) {
                    loadUrlSafely(inputUrl)
                } else {
                    hasReceivedMainFrameError = false
                    binding.layoutError.visibility = View.GONE
                    wv.reload()
                }
            }
            hideKeyboard()
        }

        binding.btnRetry.setOnClickListener {
            hasReceivedMainFrameError = false
            binding.layoutError.visibility = View.GONE
            binding.webView.reload()
        }

        binding.btnWebBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            }
        }

        binding.btnWebForward.setOnClickListener {
            if (binding.webView.canGoForward()) {
                binding.webView.goForward()
            }
        }

        binding.btnWebHome.setOnClickListener {
            loadUrlSafely(URL_TEXT2)
        }

        binding.btnClearCache.setOnClickListener {
            binding.webView.clearCache(true)
            binding.webView.clearFormData()
            showToast("已清除 WebView 快取")
        }
    }

    private fun initSourcePresets() {
        val binding = getBinding() ?: return
        binding.btnSourceText2.setOnClickListener { loadUrlSafely(URL_TEXT2) }
        binding.btnSourceText1.setOnClickListener { loadUrlSafely(URL_TEXT1) }
        binding.btnSourceQa.setOnClickListener { loadUrlSafely(URL_QA) }
        binding.btnSourceAaa.setOnClickListener { loadUrlSafely(URL_AAA) }
        binding.btnSourceGoogle.setOnClickListener { loadUrlSafely(URL_GOOGLE) }
    }

    private fun initJsBridgeControls() {
        val binding = getBinding() ?: return
        binding.btnCallJs.setOnClickListener {
            val payload = binding.etJsPayload.text.toString().ifBlank { "Hello JS!" }
            invokeJavascriptGreeting(payload, showToastOnResult = true)
            hideKeyboard()
        }
    }

    private fun configureWebView() {
        val binding = getBinding() ?: return
        val wv = binding.webView

        WebSettingsUtils.setDefaultWebSettings(context, wv)
        wv.settings.apply {
            javaScriptCanOpenWindowsAutomatically = true
            allowContentAccess = true
            blockNetworkImage = false
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }

        myWebViewClient = object : MyWebViewClient(activity) {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleExternalOrCustomScheme(url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    hasReceivedMainFrameError = true
                    val errDesc = error?.description?.toString() ?: "連線逾時或網址無效"
                    getBinding()?.let { b ->
                        b.layoutError.visibility = View.VISIBLE
                        b.tvErrorMessage.text = "載入失敗：$errDesc"
                        b.tvLoadStatusPill.text = "載入失敗"
                        b.tvLoadStatusPill.setTextColor(0xFFD32F2F.toInt())
                    }
                }
            }
        }.also { client ->
            client.setOnWebViewClientl(this)
            wv.webViewClient = client
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                val b = getBinding() ?: return
                updateLoadingUi(newProgress in 1..99, newProgress)
                if (newProgress == 100 && !hasReceivedMainFrameError) {
                    b.tvLoadStatusPill.text = "載入完成"
                    b.tvLoadStatusPill.setTextColor(0xFF019889.toInt())
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                val b = getBinding() ?: return
                if (!title.isNullOrBlank()) {
                    b.tvPageTitle.text = title
                }
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                val ctx = context ?: return super.onJsAlert(view, url, message, result)
                AlertDialog.Builder(ctx)
                    .setTitle("網頁提示")
                    .setMessage(message ?: "")
                    .setPositiveButton("確定") { _, _ -> result?.confirm() }
                    .setOnCancelListener { result?.cancel() }
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                val ctx = context ?: return super.onJsConfirm(view, url, message, result)
                AlertDialog.Builder(ctx)
                    .setTitle("網頁確認")
                    .setMessage(message ?: "")
                    .setPositiveButton("確定") { _, _ -> result?.confirm() }
                    .setNegativeButton("取消") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .show()
                return true
            }
        }

        wv.addJavascriptInterface(AndroidApp(), "AndroidApp")
    }

    private fun handleExternalOrCustomScheme(url: String): Boolean {
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
            return false
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        } catch (_: Exception) {
            showToast("裝置不支援此連結類型")
            true
        }
    }

    private fun loadUrlSafely(rawInput: String) {
        val binding = getBinding() ?: return
        val normalized = normalizeUrl(rawInput)
        if (normalized.isEmpty()) {
            showToast("請輸入有效網址")
            return
        }
        hasReceivedMainFrameError = false
        binding.layoutError.visibility = View.GONE
        binding.etUrl.setText(normalized)
        binding.tvPageSubtitle.text = normalized
        highlightSourcePreset(normalized)
        binding.webView.loadUrl(normalized)
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("file://", ignoreCase = true) ||
                trimmed.startsWith("about:", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
    }

    private fun highlightSourcePreset(url: String) {
        val binding = getBinding() ?: return
        binding.btnSourceText2.isSelected = url == URL_TEXT2
        binding.btnSourceText1.isSelected = url == URL_TEXT1
        binding.btnSourceQa.isSelected = url == URL_QA
        binding.btnSourceAaa.isSelected = url == URL_AAA
        binding.btnSourceGoogle.isSelected = url.startsWith(URL_GOOGLE)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        val binding = getBinding() ?: return
        isPageLoading = true
        if (!url.isNullOrBlank()) {
            binding.etUrl.setText(url)
            binding.tvPageSubtitle.text = url
            highlightSourcePreset(url)
        }
        updateLoadingUi(true, 15)
        updateNavigationButtons()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        val binding = getBinding() ?: return
        isPageLoading = false
        updateLoadingUi(false, 100)
        updateNavigationButtons()

        if (url == URL_TEXT2 || url == URL_TEXT1) {
            invokeJavascriptGreeting("我來自 Android 原生端", showToastOnResult = false)
        }
    }

    private fun invokeJavascriptGreeting(message: String, showToastOnResult: Boolean) {
        val binding = getBinding() ?: return
        val quoted = JSONObject.quote(message)
        val js = "javascript:if (typeof javatojswith === 'function') { javatojswith($quoted); } else { 'NO_SUCH_FUNCTION'; }"
        appendBridgeLog("Native → JS: javatojswith($quoted)")
        binding.webView.evaluateJavascript(js) { value ->
            val cleaned = value?.trim('"') ?: "null"
            if (cleaned == "NO_SUCH_FUNCTION") {
                appendBridgeLog("JS 回應：此頁面未定義 javatojswith() 方法")
                if (showToastOnResult) {
                    showToast("目前網頁未定義 javatojswith() 函式")
                }
            } else {
                appendBridgeLog("JS 回傳值：$cleaned")
                if (showToastOnResult) {
                    showToast("JS 執行成功：$cleaned")
                }
            }
        }
    }

    private fun updateLoadingUi(loading: Boolean, progress: Int) {
        val binding = getBinding() ?: return
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.progressBar.progress = progress
        binding.btnReloadOrStop.setImageResource(
            if (loading) R.drawable.ic_stop else R.drawable.ic_refresh
        )
        if (loading) {
            binding.tvLoadStatusPill.text = "載入 $progress%"
            binding.tvLoadStatusPill.setTextColor(0xFFEF6C00.toInt())
        }
    }

    private fun updateNavigationButtons() {
        val binding = getBinding() ?: return
        val canBack = binding.webView.canGoBack()
        val canForward = binding.webView.canGoForward()
        binding.btnWebBack.isEnabled = canBack
        binding.btnWebBack.alpha = if (canBack) 1.0f else 0.35f
        binding.btnWebForward.isEnabled = canForward
        binding.btnWebForward.alpha = if (canForward) 1.0f else 0.35f
    }

    private fun appendBridgeLog(entry: String) {
        val binding = getBinding() ?: return
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        if (logLines.size >= 5) {
            logLines.removeFirst()
        }
        logLines.addLast("[$timestamp] $entry")
        binding.tvBridgeLog.text = logLines.joinToString("\n")
    }

    private fun hideKeyboard() {
        val view = view ?: return
        val imm = context?.getSystemService<InputMethodManager>()
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private inner class AndroidApp {
        @JavascriptInterface
        fun JsToJavaInterface(value: String?) {
            val safeMessage = value ?: "(empty)"
            mainHandler.post {
                if (!isAdded || view == null) return@post
                appendBridgeLog("JS → Native: $safeMessage")
                showToast("收到 JS 訊息：$safeMessage")
            }
        }
    }
}