package com.niles.ip_camera;

import android.text.TextUtils;
import android.view.Surface;

import java.io.File;

/**
 * Created by Niles
 * Date 2018/10/11 10:55
 * Email niulinguo@163.com
 */
public class VideoStreamConfig {

    private final String mIP;
    private final int mPort;
    private final int mChannel;
    private final String mStreamType;
    private final String mUsername;
    private final String mPassword;
    private final Surface mSurface;
    private final int mDuration;
    private final File mVideoFile;
    private final VideoResultCallback mVideoResultCallback;

    private VideoStreamConfig(String IP, int port, int channel, String streamType, String username, String password, Surface surface, int duration, File videoFile, VideoResultCallback videoResultCallback) {
        mIP = IP;
        mPort = port;
        mChannel = channel;
        mStreamType = streamType;
        mUsername = username;
        mPassword = password;
        mSurface = surface;
        mDuration = duration;
        mVideoFile = videoFile;
        mVideoResultCallback = videoResultCallback;
    }

    public VideoResultCallback getVideoResultCallback() {
        return mVideoResultCallback;
    }

    public Surface getSurface() {
        return mSurface;
    }

    public File getVideoFile() {
        return mVideoFile;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getIP() {
        return mIP;
    }

    public int getPort() {
        return mPort;
    }

    public int getChannel() {
        return mChannel;
    }

    public String getStreamType() {
        return mStreamType;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getUsername() {
        return mUsername;
    }

    public static final class Builder {

        private String mIP;
        private int mPort = 80;
        private int mChannel = 11;
        private String mStreamType = "video";
        private String mUsername = "admin";
        private String mPassword = "admin";
        private Surface mSurface;
        private int mDuration;
        private File mVideoFile;
        private VideoResultCallback mVideoResultCallback;

        public String getIP() {
            return mIP;
        }

        public Builder setIP(String IP) {
            mIP = IP;
            return this;
        }

        public VideoResultCallback getVideoResultCallback() {
            return mVideoResultCallback;
        }

        public Builder setVideoResultCallback(VideoResultCallback videoResultCallback) {
            mVideoResultCallback = videoResultCallback;
            return this;
        }

        public File getVideoFile() {
            return mVideoFile;
        }

        public Builder setVideoFile(File videoFile) {
            mVideoFile = videoFile;
            return this;
        }

        public int getDuration() {
            return mDuration;
        }

        public Builder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public Surface getSurface() {
            return mSurface;
        }

        public Builder setSurface(Surface surface) {
            mSurface = surface;
            return this;
        }

        public int getPort() {
            return mPort;
        }

        public Builder setPort(int port) {
            mPort = port;
            return this;
        }

        public int getChannel() {
            return mChannel;
        }

        public Builder setChannel(int channel) {
            mChannel = channel;
            return this;
        }

        public String getStreamType() {
            return mStreamType;
        }

        public Builder setStreamType(String streamType) {
            mStreamType = streamType;
            return this;
        }

        public String getUsername() {
            return mUsername;
        }

        public Builder setUsername(String username) {
            mUsername = username;
            return this;
        }

        public String getPassword() {
            return mPassword;
        }

        public Builder setPassword(String password) {
            mPassword = password;
            return this;
        }

        public VideoStreamConfig build() {

            check();

            return new VideoStreamConfig(
                    mIP,
                    mPort,
                    mChannel,
                    mStreamType,
                    mUsername,
                    mPassword,
                    mSurface,
                    mDuration,
                    mVideoFile,
                    mVideoResultCallback
            );
        }

        private void check() {

            if (TextUtils.isEmpty(mIP)) {
                throw new RuntimeException("IP Is Null");
            }

            if (mDuration < 3) {
                mDuration = 3;
            }

            if (mDuration > 30) {
                mDuration = 30;
            }

            if (mVideoFile == null) {
                throw new RuntimeException("Video File Is Null");
            }

            if (mVideoResultCallback == null) {
                throw new RuntimeException("Video Result Callback Is Null");
            }
        }
    }
}
