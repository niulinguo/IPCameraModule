package com.niles.ip_camera;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Niles
 * Date 2018/10/9 13:55
 * Email niulinguo@163.com
 */
public interface CgiService {

    @GET("/cgi-bin/hi3510/param.cgi")
    Call<String> paramCgi(@QueryMap Map<String, String> options);

    @GET("/tmpfs/snap.jpg")
    Call<ResponseBody> getSnapImage(@QueryMap Map<String, String> options);
}
