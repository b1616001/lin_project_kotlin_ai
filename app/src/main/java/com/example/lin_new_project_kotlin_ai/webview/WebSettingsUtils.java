package com.example.lin_new_project_kotlin_ai.webview;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebSettingsUtils {
    private static final String APP_CACHE_DIRNAME = "/webcache"; // web缓存目录

    public static void setDefaultWebSettings(Context context, WebView webView) {
        WebSettings webSettings = webView.getSettings();
        //5.0以上开启混合模式加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//設定js可以直接開啟視窗，如window.open()，預設為false

        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        //允许js代码
        webSettings.setJavaScriptEnabled(true);
        //允许SessionStorage/LocalStorage存储
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);        //设置缓存模式

        //禁用放缩
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(false);
        //禁用文字缩放
        webSettings.setTextZoom(100);
        //10M缓存，api 18后，系统自动管理。
//        webSettings.setAppCacheMaxSize(10 * 1024 * 1024);
        //允许缓存，设置缓存位置
//        webSettings.setAppCacheEnabled(true);//被刪除方法了
//        webSettings.setAppCachePath(context.getDir("appcache", 0).getPath());//被刪除方法了
        //允许WebView使用File协议
        webSettings.setAllowFileAccess(true);
        //不保存密码
        webSettings.setSavePassword(false);
        //设置UA
//        webSettings.setUserAgentString(webSettings.getUserAgentString() + " kaolaApp/" + AppUtils.getVersionName());
        //移除部分系统JavaScript接口
//        KaolaWebViewSecurity.removeJavascriptInterfaces(webView);
        //自动加载图片
//        webSettings.setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= 19 ) {
            webView.getSettings().setLoadsImagesAutomatically( true );
        } else {
            webView.getSettings().setLoadsImagesAutomatically( false );
        }
        // 开启DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        // 开启database storage API功能
        webSettings.setDatabaseEnabled(true);
    }

    public static void setCache(Context context, WebView webView) {
        WebSettings settings = webView.getSettings();
        //设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        String cacheDirPath = context.getFilesDir().getAbsolutePath() + APP_CACHE_DIRNAME;
        Log.i("cachePath", cacheDirPath);
        // 设置数据库缓存路径
//        settings.setAppCachePath(cacheDirPath);
//        settings.setAppCacheMaxSize(20*1024*1024);//        // 2. 設定快取大小


//        settings.setAppCacheEnabled(true);

        Log.i("databasepath", settings.getDatabasePath());
    }

}
