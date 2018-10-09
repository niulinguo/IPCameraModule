package com.niles.ipcameramodule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.niles.http.HttpConfig;
import com.niles.ip_camera.ApiManager;

import java.util.HashMap;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final HttpConfig mHttpConfig = new HttpConfig.Builder()
            .setBaseUrl("http://10.0.66.222")
            .setLogger(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.e("http", message);
                }
            })
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiManager.setHttpConfig(mHttpConfig);

        HotspotManager manager = new HotspotManager();
        manager.refresh();

        List<HotspotClientInfo> infoList = manager.getInfoList();

        HashMap<String, String> options = new HashMap<>();
        options.put("-dd", "vv");
        Call<String> call = ApiManager.paramCgi("setldcattr", options);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.e("result", response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("result", t.getMessage());
            }
        });
    }
}
