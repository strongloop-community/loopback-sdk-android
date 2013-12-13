package com.strongloop.android.loopback;

public class Installation extends Model {

    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String STATUS_ACTIVE = "Active";

    String appId;
    String appVersion;
    String userId;
    String deviceToken;
    String status;

    public String getAppId() {
        return appId;
    }
    public void setAppId(final String appId) {
        this.appId = appId;
    }
    public String getAppVersion() {
        return appVersion;
    }
    public void setAppVersion(final String appVersion) {
        this.appVersion = appVersion;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(final String userId) {
        this.userId = userId;
    }
    public String getDeviceType() {
        return DEVICE_TYPE_ANDROID;
    }
    public String getDeviceToken() {
        return deviceToken;
    }
    public void setDeviceToken(final String deviceToken) {
        this.deviceToken = deviceToken;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(final String status) {
        this.status = status;
    }
}
