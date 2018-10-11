package com.niles.ip_camera;

import com.niles.http.HttpManager;

import java.util.HashMap;
import java.util.LinkedHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Niles
 * Date 2018/10/9 16:39
 * Email niulinguo@163.com
 */
public class CameraApiManager {

    private static HttpManager sHttpManager;
    private static String sUsername;
    private static String sPassword;

    public static void setHttpManager(HttpManager httpManager) {
        sHttpManager = httpManager;
    }

    public static void setPassword(String password) {
        sPassword = password;
    }

    public static void setUsername(String username) {
        sUsername = username;
    }

    public static Call<String> paramCgi(String cmd, HashMap<String, String> options) {
        HashMap<String, String> newOptions = new LinkedHashMap<>();
        newOptions.put("cmd", cmd);
        newOptions.put("-usr", sUsername);
        newOptions.put("-pwd", sPassword);
        if (options != null && !options.isEmpty()) {
            newOptions.putAll(options);
        }
        return sHttpManager.createService(CgiService.class).paramCgi(newOptions);
    }

    public static Call<String> getVideoAttr() {
        return paramCgi("getvideoattr", null);
    }

    public static Call<String> snapImage() {
        return paramCgi("snapimage", null);
    }

    public static Call<ResponseBody> getSnapImage() {
        HashMap<String, String> options = new LinkedHashMap<>();
        options.put("-usr", sUsername);
        options.put("-pwd", sPassword);
        return sHttpManager.createService(CgiService.class).getSnapImage(options);
    }

    public static Call<ResponseBody> getAutoImage() {
        HashMap<String, String> options = new LinkedHashMap<>();
        options.put("-usr", sUsername);
        options.put("-pwd", sPassword);
        return sHttpManager.createService(CgiService.class).getAutoImage(options);
    }

    public static Call<String> getHttpPort() {
        return paramCgi("gethttpport", null);
    }
}
