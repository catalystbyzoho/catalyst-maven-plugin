package com.zoho.catalyst.auth;

import com.zoho.catalyst.enums.AuthType;
import com.zoho.catalyst.enums.DC;
import com.zoho.catalyst.enums.Environment;

public interface ICatalystAuth {
    String getAccessToken(boolean forceRefresh) throws Exception;
    AuthType getAuthType();
    Environment getEnvironment();
    DC getDC();
}
