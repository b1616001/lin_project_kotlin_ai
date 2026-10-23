package com.example.lin_new_project_kotlin_ai.webService;

import static com.example.lin_new_project_kotlin_ai.fun.CommonUtils.isConnected;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lin_new_project_kotlin_ai.webService.DefaultAdapter.DoubleDefaultAdapter;
import com.example.lin_new_project_kotlin_ai.webService.DefaultAdapter.IntegerDefaultAdapter;
import com.example.lin_new_project_kotlin_ai.webService.DefaultAdapter.LongDefaultAdapter;
import com.example.lin_new_project_kotlin_ai.webService.DefaultAdapter.StringNullAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class WebApi {
    private static Context context;
    private static String TAG = "WebApi";
    private static String LOCAL_SERVER_URL = "https://cleaner.epb.taichung.gov.tw/TGService/CleanerWebService.asmx/";//正式URL，從API來

    public static void initialization(Context mContext){
        context=mContext;
    }
    public static <T> T createApi(Class<T> service) {
        //默認情況下，Gson是嚴格的，只接受RFC 4627指定的JSON。此選項使解析器在接受的內容中更加自由。
        //    Gson gson = new GsonBu;;ilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LOCAL_SERVER_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) //增加返回值為String的支持(要排第一)
                .addConverterFactory(GsonConverterFactory.create(buildGson()))//增加返回值為为GSON的支持(以實體類返回)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//增加返回值為Observable<T>的支持
                .client(getOkhttp())
                .build();
        return retrofit.create(service);
    }

    public static OkHttpClient getOkhttp() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
//                        .addHeader("brandCode", brandCode)
//                        .addHeader("app", "HOME_OLD_MACHINE_CONSOLE")
                        .build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(6, TimeUnit.SECONDS)
                // 設置讀寫時間
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        return mOkHttpClient;
    }
    public static Gson buildGson() {
        return  new GsonBuilder()
                .registerTypeAdapter(Integer.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(int.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(Double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(Long.class, new LongDefaultAdapter())
                .registerTypeAdapter(long.class, new LongDefaultAdapter())
                .registerTypeAdapter(String.class, new StringNullAdapter())
                .create();
    }

    public static <T> void request(Observable<T> observable, final IResponseListener<T> listener) {
        final CompositeDisposable compositeDisposable = new CompositeDisposable();
        if (!isConnected(context)) {
            if (listener != null) {
                setLog("無網路");
                listener.onFail();
            }
            return;
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                               @Override
                               public void onError(@NonNull Throwable e) {
                                   e.printStackTrace();
                                   setLog("onError:"+ Objects.requireNonNull(e.getMessage()));
                                   if (listener != null) {
                                       listener.onFail();
                                   }
                               }

                               @Override
                               public void onComplete() {
                                   compositeDisposable.dispose();
                               }

                               @Override
                               public void onSubscribe(@NonNull Disposable disposable) {
                                   compositeDisposable.add(disposable);
                               }

                               @Override
                               public void onNext(@NonNull T data) {
                                   if (listener != null) {
                                       listener.onSuccess(data);
                                   }
                               }
                           }
                );
    }



    public interface IResponseListener<T> {
        void onSuccess(T data);
        void onFail();
    }
    private static void setLog(String message) {
        Log.d(TAG, message);
    }
}
