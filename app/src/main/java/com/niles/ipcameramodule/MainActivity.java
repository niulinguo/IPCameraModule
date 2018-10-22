package com.niles.ipcameramodule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.niles.http.HttpConfig;
import com.niles.http.HttpManager;
import com.niles.http.converter.StringConverterFactory;
import com.niles.ip_camera.CameraApiManager;
import com.niles.ip_camera.VideoResultCallback;
import com.niles.ip_camera.VideoStream;
import com.niles.ip_camera.VideoStreamConfig;
import com.niles.ip_camera.hotspot.HotspotClientInfo;
import com.niles.ip_camera.hotspot.HotspotManager;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String MAC = "14:6b:9c:b7:a1:f2";

    private TextView mTextView;
    private ImageView mImageView;

    private SurfaceView mSurfaceView;

    private int mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tv_text);
        mImageView = findViewById(R.id.iv_image);
        mSurfaceView = findViewById(R.id.surface_view);

        HotspotManager manager = new HotspotManager();
        manager.refresh();
        HotspotClientInfo clientInfo = manager.find(MAC);

        if (clientInfo == null) {
            Toast.makeText(this, "没有找到IP_CAMERA", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String ipAddress = clientInfo.getIPAddress();

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
        CameraApiManager.setHttpManager(httpManager);
        CameraApiManager.setUsername("admin");
        CameraApiManager.setPassword("admin");

//        CameraApiManager.getHttpPort().enqueue(new Callback<String>() {
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

        mNumber = 0;
    }

    private void autoImage() {
        mNumber += 1;
        mTextView.setText(String.valueOf(mNumber));
        CameraApiManager.getAutoImage().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try {
                        byte[] bytes = responseBody.bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mImageView.setImageBitmap(bitmap);
                        autoImage();
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

    private void snapImage() {
        CameraApiManager.snapImage().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String body = response.body();
                Log.e("result", body);
                if ("[Succeed]get ok.".equalsIgnoreCase(body)) {
                    CameraApiManager.getSnapImage().enqueue(new Callback<ResponseBody>() {
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onButtonClicked(View view) {
        VideoStream.showVideo(new VideoStreamConfig.Builder()
                .setIP(mTextView.getText().toString())
                .setSurface(mSurfaceView.getHolder().getSurface())
                .setDuration(10)
                .setVideoFile(new File(new File(Environment.getExternalStorageDirectory(), "IPCamera"), "video.h264"))
                .setVideoResultCallback(new VideoResultCallback() {
                    @Override
                    public void onFailure(final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final File videoFile) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, videoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .build());
//        snapImage();
    }
}
