package com.interjoy.skrobotobject.util;

import com.interjoy.skrobotobject.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by zhangwenwei on 2017/6/24.
 */

public class OkHttpUtil {
    private static OkHttpClient mOkHttpClient = null;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(8, TimeUnit.SECONDS);
        builder.readTimeout(8, TimeUnit.SECONDS);
        builder.writeTimeout(8, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        mOkHttpClient = builder.build();


    }

    public static Call enqueue(Request request) {
        return mOkHttpClient.newCall(request);
    }


    public static OkHttpClient.Builder getCopyOk() {
        return mOkHttpClient.newBuilder();
    }


}
