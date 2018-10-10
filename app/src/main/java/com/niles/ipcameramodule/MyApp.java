package com.niles.ipcameramodule;

import android.app.Application;

import com.niles.http.HttpManager;

/**
 * Created by Niles
 * Date 2018/10/10 12:49
 * Email niulinguo@163.com
 */
public class MyApp extends Application {

    private static MyApp sInstance;
    private HttpManager mHttpManager = new HttpManager();

    public static MyApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public HttpManager getHttpManager() {
        return mHttpManager;
    }
}
