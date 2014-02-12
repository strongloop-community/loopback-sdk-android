package com.strongloop.android.loopback;

public class AccessToken extends Model {

    private String userId;
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    String getUserId() {
        return userId;
    }
}
