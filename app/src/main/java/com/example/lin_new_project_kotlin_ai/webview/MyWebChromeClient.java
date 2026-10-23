package com.example.lin_new_project_kotlin_ai.webview;

import android.content.Context;
import android.view.View;
import android.webkit.WebChromeClient;

public class MyWebChromeClient extends WebChromeClient {
    private OnWebChromeClient onWebChromeClient;
    public MyWebChromeClient(Context context){
        if (context instanceof OnWebChromeClient) {
            this.onWebChromeClient = (OnWebChromeClient) context;
        }
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        onWebChromeClient.onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();
        onWebChromeClient.onHideCustomView();
    }
    public interface OnWebChromeClient {
        void onShowCustomView(View view, CustomViewCallback callback);
        void onHideCustomView();
    }
}
