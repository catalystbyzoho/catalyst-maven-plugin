package com.zoho.catalyst.auth;

import com.zoho.catalyst.enums.AuthType;
import com.zoho.catalyst.enums.DC;
import com.zoho.catalyst.enums.Environment;
import com.zoho.catalyst.pojo.CatalystAuthConfig;

import java.util.HashMap;

public class Authenticator implements ICatalystAuth {
    public static HashMap<AuthType, Authenticator> authMap = new HashMap<>();

    private final CatalystAuthConfig authConfig;

    protected Authenticator(CatalystAuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    public String getAccessToken() throws Exception {
        return getAccessToken(false);
    }

    @Override
    public String getAccessToken(boolean forceRefresh) throws Exception {
        return null;
    }

    public AuthType getAuthType() {
        return authConfig.getType();
    }

    public Environment getEnvironment() {
        return authConfig.getEnv();
    }

    public DC getDC() {
        return authConfig.getDc();
    }

    public static Authenticator initialize(CatalystAuthConfig config) throws Exception {
        AuthType type = config.getType();
        if(authMap.containsKey(type)) {
            return authMap.get(type);
        }
        Authenticator authenticator;
        switch (type) {
            case ZCATALYST_CLI:
                authenticator = new CatalystCliAuth(config);
                break;
            default:
                authenticator = new Oauth2Auth(config);
        }
        authMap.put(type, authenticator);
        return authenticator;
    }
}
