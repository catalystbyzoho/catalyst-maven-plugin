package com.zoho.catalyst.Interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CatalystHttpInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request reqWithHeaders = chain
                .request()
                .newBuilder()
                .addHeader("Accept", "application/vnd.catalyst.v2+json")
                .addHeader("User-Agent", "catalyst-maven-plugin/{version}")
                .addHeader("X-CATALYST-Environment", "Development")
                .build();
        return chain.proceed(reqWithHeaders);
    }
}
