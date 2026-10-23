package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import com.example.lin_new_project_kotlin_ai.databinding.FragmentWebviewBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.example.lin_new_project_kotlin_ai.webview.MyWebViewClient
import com.example.lin_new_project_kotlin_ai.webview.MyWebViewClient.OnWebViewClient
import com.example.lin_new_project_kotlin_ai.webview.WebSettingsUtils


class WebViewFragment : BaseBindingFragment<FragmentWebviewBinding?>(), OnWebViewClient {
    var myWebViewClient: MyWebViewClient? = null
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater
    ): FragmentWebviewBinding {
        return FragmentWebviewBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        myWebViewClient = MyWebViewClient(getActivity())
        myWebViewClient!!.setOnWebViewClientl(this)
        getBinding()!!.webView.clearCache(true)
        getBinding()!!.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true)
        getBinding()!!.webView.getSettings().setAllowContentAccess(true)
        getBinding()!!.webView.setWebViewClient(myWebViewClient!!)
        getBinding()!!.webView.getSettings().setBlockNetworkImage(false)
        WebSettingsUtils.setDefaultWebSettings(getContext(), getBinding()!!.webView)
        getBinding()!!.webView.loadUrl("file:///android_asset/text2.html")
        getBinding()!!.webView.addJavascriptInterface(
            AndroidApp(),
            "AndroidApp"
        ) //這段式測試如果從網頁回拋資料給android原生代碼範例
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        getBinding()!!.webView.evaluateJavascript(
            "javascript:javatojswith('我来自Jav51212a')",
            object : ValueCallback<String?> {
                override fun onReceiveValue(value: String?) {
                    if (value != null) {
                        Log.d("avascript回傳", value)
                    }
                }
            })
    }

    private inner class AndroidApp {
        @JavascriptInterface //這個方法名稱是由根後端工程師討論定義下來的 可變動
        fun JsToJavaInterface(value: String?) {
            Log.d("順序", "JsToJavaInterface")

            ToastMethod.showToast(requireContext(), value)
        }
    }
}