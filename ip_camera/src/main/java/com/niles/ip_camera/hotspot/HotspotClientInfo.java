package com.niles.ip_camera.hotspot;

/**
 * Created by Niles
 * Date 2018/10/9 13:04
 * Email niulinguo@163.com
 */
public class HotspotClientInfo {

    private final String mIPAddress;
    private final String mHWType;
    private final String mFlags;
    private final String mHWAddress;
    private final String mMask;
    private final String mDevice;

    public HotspotClientInfo(String IPAddress, String HWType, String flags, String HWAddress, String mask, String device) {
        mIPAddress = IPAddress;
        mHWType = HWType;
        mFlags = flags;
        mHWAddress = HWAddress;
        mMask = mask;
        mDevice = device;
    }

    public String getIPAddress() {
        return mIPAddress;
    }

    public String getHWType() {
        return mHWType;
    }

    public String getFlags() {
        return mFlags;
    }

    public String getHWAddress() {
        return mHWAddress;
    }

    public String getMask() {
        return mMask;
    }

    public String getDevice() {
        return mDevice;
    }

    public static final class Builder {

        private String mIPAddress;
        private String mHWType;
        private String mFlags;
        private String mHWAddress;
        private String mMask;
        private String mDevice;

        public String getIPAddress() {
            return mIPAddress;
        }

        public Builder setIPAddress(String IPAddress) {
            mIPAddress = IPAddress;
            return this;
        }

        public String getHWType() {
            return mHWType;
        }

        public Builder setHWType(String HWType) {
            mHWType = HWType;
            return this;
        }

        public String getFlags() {
            return mFlags;
        }

        public Builder setFlags(String flags) {
            mFlags = flags;
            return this;
        }

        public String getHWAddress() {
            return mHWAddress;
        }

        public Builder setHWAddress(String HWAddress) {
            mHWAddress = HWAddress;
            return this;
        }

        public String getMask() {
            return mMask;
        }

        public Builder setMask(String mask) {
            mMask = mask;
            return this;
        }

        public String getDevice() {
            return mDevice;
        }

        public Builder setDevice(String device) {
            mDevice = device;
            return this;
        }

        public HotspotClientInfo build() {
            return new HotspotClientInfo(
                    mIPAddress,
                    mHWType,
                    mFlags,
                    mHWAddress,
                    mMask,
                    mDevice
            );
        }
    }
}
