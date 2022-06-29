package com.zoho.catalyst.Interceptor;

import com.zoho.catalyst.auth.Authenticator;
import com.zoho.catalyst.pojo.CatalystAuthConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CatalystAuthorizationInterceptor implements Interceptor {
    private CatalystAuthConfig auth;
    public CatalystAuthorizationInterceptor(CatalystAuthConfig auth) {
        this.auth = auth;
    }
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        try {
            String accessToken = Authenticator.initialize(auth).getAccessToken();
            Request reqWithAuth = chain
                    .request()
                    .newBuilder()
                    .addHeader("authorization", "Bearer " + accessToken)
                    .build();
            return chain.proceed(reqWithAuth);
        }
        catch(Exception e) {
            throw new IOException("Unable to authenticate request", e);
        }
    }
}
