package com.niles.ipcameramodule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.niles.http.HttpConfig;
import com.niles.http.HttpManager;
import com.niles.http.converter.StringConverterFactory;
import com.niles.ip_camera.ApiManager;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String MAC = "14:6b:9c:b7:a1:f2";

    private TextView mTextView;
    private ImageView mImageView;

//    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tv_text);
        mImageView = findViewById(R.id.iv_image);
//        mVideoView = findViewById(R.id.vv_video);

        HotspotManager manager = new HotspotManager();
        manager.refresh();

        List<HotspotClientInfo> infoList = manager.getInfoList();

        String ipAddress = null;

        for (HotspotClientInfo info : infoList) {
            if (MAC.equalsIgnoreCase(info.getHWAddress())) {
                ipAddress = info.getIPAddress();
            }
        }

        if (TextUtils.isEmpty(ipAddress)) {
            Toast.makeText(this, "没有找到IP_CAMERA", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mTextView.setText(ipAddress);

        HttpManager httpManager = MyApp.getInstance().getHttpManager();
        httpManager.setHttpConfig(new HttpConfig.Builder()
                .setBaseUrl("http://" + ipAddress)
                .addConverterFactory(StringConverterFactory.create())
                .setLogger(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.e("http", message);
                    }
                })
                .build());
        ApiManager.setHttpManager(httpManager);
        ApiManager.setUsername("admin");
        ApiManager.setPassword("admin");

//        ApiManager.getHttpPort().enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                String body = response.body();
//                Log.e("result", body);
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//            }
//        });
    }

    public void onButtonClicked(View view) {

//        String url = "http://" + mTextView.getText().toString() + "/livestream/11?action=play&media=video";
//        url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//        url = "http://ivi.bupt.edu.cn/hls/cctv5phd.m3u8";
//        HashMap<String, String> headers = new HashMap<>();
//        headers.put("User-Agent", "HiIpcam/V100R003 VodClient/1.0.0");
//        headers.put("Connection", "Keep-Alive");
//        headers.put("Cache-Control", "no-cache");
//        headers.put("Authorization", "admin admin");
//        headers.put("Content-Length", "65535");
//        headers.put("Cseq", "1");
//        headers.put("Transport", "RTP/AVP/TCP;unicast;interleaved=0-1");
//        mVideoView.setVideoURI(Uri.parse(url), headers);
//        mVideoView.set();
//        mVideoView.setVideoURI(Uri.parse(url));
//        mVideoView.start();


        ApiManager.snapImage().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String body = response.body();
                Log.e("result", body);
                if ("[Succeed]get ok.".equalsIgnoreCase(body)) {
                    ApiManager.getSnapImage().enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                try {
                                    byte[] bytes = responseBody.bytes();
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    mImageView.setImageBitmap(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("http", "responseBody is null");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e("result", t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("result", t.getMessage());
            }
        });
    }
}
