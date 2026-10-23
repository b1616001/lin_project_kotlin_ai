package com.example.lin_new_project_kotlin_ai.webview;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWebViewClient extends WebViewClient {
    private OnWebViewClient onWebViewClientl;



    public MyWebViewClient(Context context){
        if (context instanceof OnWebViewClient) {
            this.onWebViewClientl = (OnWebViewClient) context;
        }
    }

    public void setOnWebViewClientl(OnWebViewClient onWebViewClientl) {
        this.onWebViewClientl = onWebViewClientl;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        Uri uri = request.getUrl();
//        if (uri != null) {
//            WebResourceResponse response = getWebResourceResponse(request);
//
//
//            if (response != null) {
//                try {
//                    Log.d("WebResourceResponseM2",convertToString(response.getData()));
//
//                }catch (Exception e){
//
//                }
//                Log.d("WebResourceResponse",new Gson().toJson(response));
////                return response;
//
//
//            }else {
//                Log.d("WebResourceResponse","response.toString()");
//
//            }
//        }
        return super.shouldInterceptRequest(view, request);
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view,  WebResourceRequest request) {
//        Log.d("responseURL",request.getUrl().toString());
        return super.shouldOverrideUrlLoading(view, request);

    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        Log.d("responseURL",url);
//        onWebViewClientl.onPageFinished(com.example.lin_new_project_kotlin.view, url+"AAAAA");
//        com.example.lin_new_project_kotlin.view.loadUrl(url);
//        return true;
                return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {

//            Log.i("WebViewClient", "onLoadResource url="+url);
        //com.example.lin_new_project_kotlin.view WebView：正在啟動回調的WebView。
        //url String：該資源的WebView將加載的URL。
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        onWebViewClientl.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        onWebViewClientl.onPageFinished(view, url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    public interface OnWebViewClient {
        void onPageStarted(WebView view, String url, Bitmap favicon);
        void onPageFinished(WebView view, String url);
    }
    @Nullable
    private WebResourceResponse getWebResourceResponse(WebResourceRequest request) {
        try {
            final String method = request.getMethod();
            final String url = request.getUrl().toString();
            String ext = MimeTypeMap.getFileExtensionFromUrl(url);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

            if (url != null &&url.equals("https://dtbjv88.tyhlggj.cn/")) {
                HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Sample-Header", "hello");
                conn.setDoInput(true);
                conn.setUseCaches(false);

                Map<String, String> responseHeaders = convertResponseHeaders(conn.getHeaderFields());

                responseHeaders.put("Sample-Header", "hello");

                return new WebResourceResponse(
                        mime,
                        conn.getContentEncoding(),
                        conn.getResponseCode(),
                        conn.getResponseMessage(),
                        responseHeaders,
                        conn.getInputStream()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private Map<String, String> convertResponseHeaders(Map<String, List<String>> headers) {
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Sample-Header", "hello");

        for (Map.Entry<String, List<String>> item : headers.entrySet()) {
            List<String> values = new ArrayList<String>();

            for (String headerVal : item.getValue()) {
                values.add(headerVal);
            }
            String value = StringUtils.join(values, ",");
            Log.e(TAG, "processRequest: " + item.getKey() + " : " + value);

            responseHeaders.put(item.getKey(), value);
        }

        return responseHeaders;
    }
    public String convertToString(InputStream inputStream){
        StringBuffer string = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                string.append(line + "\n");
                Log.d("測試",string.toString());
            }
        } catch (Exception e) {}
        return string.toString();
    }
}
