package com.niles.ip_camera;

import java.io.File;

/**
 * Created by Niles
 * Date 2018/10/22 12:40
 * Email niulinguo@163.com
 */
public interface VideoResultCallback {

    void onFailure(String message);

    void onSuccess(File videoFile);
}
