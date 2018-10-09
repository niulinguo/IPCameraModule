package com.niles.ip_camera;

import com.niles.http.HttpConfig;
import com.niles.http.RetrofitApiFactory;

import java.util.HashMap;

import retrofit2.Call;

/**
 * Created by Niles
 * Date 2018/10/9 16:39
 * Email niulinguo@163.com
 */
public class ApiManager {

    private static HttpConfig sHttpConfig;

    public static void setHttpConfig(HttpConfig httpConfig) {
        sHttpConfig = httpConfig;
    }

    public static <S> S create(Class<S> serviceClass) {
        return RetrofitApiFactory.create(sHttpConfig, serviceClass);
    }

    public static Call<String> paramCgi(String cmd, HashMap<String, String> options) {
        options.put("cmd", cmd);
        return create(CgiService.class).paramCgi(options);
    }
}
