package com.niles.ipcameramodule;

import android.Manifest;
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

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.PermissionUtils;
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
import java.io.InputStream;
import java.util.HashMap;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionUtils.permission(PermissionConstants.STORAGE)
                        .callback(new PermissionUtils.SimpleCallback() {
                            @Override
                            public void onGranted() {

                            }

                            @Override
                            public void onDenied() {
                                finish();
                            }
                        })
                        .request();
            }
        }


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

        getImageAttr();

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
                                InputStream inputStream = responseBody.byteStream();
                                File imageFile = getImageFile();
                                FileIOUtils.writeFileFromIS(imageFile, inputStream);
                                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                mImageView.setImageBitmap(bitmap);
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

    private File getImageFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), "IPCamera");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建文件夹失败");
        }
        File file = new File(dir, System.currentTimeMillis() + ".jpg");
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("文件删除失败");
        }
        return file;
    }

    public void onButtonClicked(View view) {
        snapImage();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showVideo() {
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
    }

    private void setImageAttr() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("-flip", "on");
        paramMap.put("-mirror", "on");
        CameraApiManager.setImageAttr(paramMap).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String body = response.body();
                Log.e("result", body);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }

    private void getImageAttr() {
        CameraApiManager.getImageAttr().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String body = response.body();
                Log.e("result", body);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }
}
