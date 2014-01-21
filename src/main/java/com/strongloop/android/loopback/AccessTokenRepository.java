package com.strongloop.android.loopback;


public class AccessTokenRepository extends ModelRepository<AccessToken> {
    public AccessTokenRepository() {
        super("accessToken", AccessToken.class);
    }
}

