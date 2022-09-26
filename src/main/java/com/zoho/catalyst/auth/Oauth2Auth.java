package com.zoho.catalyst.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.zoho.catalyst.pojo.CatalystAuthConfig;
import com.zoho.catalyst.pojo.PluginCredential;
import com.zoho.catalyst.utils.AuthUtil;
import com.zoho.catalyst.utils.Url;
import lombok.extern.java.Log;
import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Externalise constants
@Log
public class Oauth2Auth extends Authenticator  {
    private static final String CONFIG_FILE_NAME = "auth.json";
    private static final String APP_NAME = "catalyst-maven-plugin";
    private static final String CLIENT_ID = "1000.D5IIHDXSPN2MII26AD0V61I6RMVSNM";
    // TODO: need to change it to {{__CATALYST_CLIENT_SECRET__}}
    private static final String CLIENT_SECRET = "02ee875ecfc50573e5cc8d62916ad3077be20d0f42";
    private static final Long MAX_EXPIRY = 15 * 60 * 1000L; // 15 min in millis
    private static PluginCredential cred = null;
    public static File getConfigFile() {
        return Paths.get(AuthUtil.getOSConfigDir(APP_NAME).toString(), CONFIG_FILE_NAME).toFile();
    }

    public static void writeCredToFile(File configFile) throws IOException {
        log.info("writing to file ::: " + configFile.getAbsolutePath());
        if(!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }
        new ObjectMapper().writeValue(configFile, cred);
        log.info("writing complete");
    }

    protected Oauth2Auth(CatalystAuthConfig authConfig) throws Exception {
        super(authConfig);
        File configFile = getConfigFile();
        if(configFile.exists()) {
            try(InputStream stream = new FileInputStream(configFile)) {
                ObjectMapper mapper = new ObjectMapper();
                cred = mapper.readValue(stream, PluginCredential.class);
                // todo: check if authConfig has the necessary data for the give DC and login if needed
            }
            return;
        }
        // login here
        log.info("login starting");
        cred = login();
        log.info("credential has been returned :::: " + cred.getAccessToken() + cred.getRefreshToken());
        writeCredToFile(configFile);
    }

    private URL getLoginUrl() {
        log.info("getting dc");
        log.info("DC" + getDC());

        Url url = new Url(getDC());
        log.info("url " + url);
        log.info("url2 " + url.getAuthUrl());
        return HttpUrl.parse(url.getAuthUrl())
                .newBuilder()
                .addPathSegments("oauth/v2/auth")
                .addQueryParameter("client_id", CLIENT_ID)
                .addQueryParameter("scope", "AaaServer.profile.READ Stratus.sdkop.CREATE Stratus.sdkop.READ ZohoCatalyst.projects.ALL ZohoCatalyst.functions.ALL ZohoCatalyst.cache.CREATE")
                .addQueryParameter("response_type", "code")
                .addQueryParameter("access_type", "offline")
                .addQueryParameter("prompt", "consent")
                .addQueryParameter("redirect_uri", "http://localhost:9005")
                .build()
                .url();
    }

    private PluginCredential getCred(String code) throws Exception {
        Url url = new Url(getDC());
        HttpUrl httpUrl = HttpUrl.parse(url.getAuthUrl())
                .newBuilder()
                .addPathSegments("oauth/v2/token")
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("redirect_uri", "http://localhost:9005")
                .add("code", code)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .post(formBody)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        ObjectMapper mapper = new ObjectMapper();
        PluginCredential cred = mapper.readValue(response.body().byteStream(), PluginCredential.class);
        response.close();
        if(cred.getAccessToken() == null || cred.getRefreshToken() == null) {
            throw new Exception("Unable to get token from code");
        }
        cred.setCreatedTime(System.currentTimeMillis());
        cred.setExpiresAt(System.currentTimeMillis() + (cred.getExpiredIn() * 1000));
        return cred;
    }

    public PluginCredential login() throws Exception {
        URL loginUrl = getLoginUrl();
        log.info("loginUrl" + loginUrl.toString());
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 9005), 0);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        server.setExecutor(executorService);
        Oauth2HttpHandler handler = new Oauth2HttpHandler();
        server.createContext("/", handler);
        server.start();
        log.info("Visit this link in a browser from current machine: ");
        log.info(loginUrl.toString());
        log.info("");
        log.info("Waiting for user action.");
        int timeMultiplier = 0;
        while(handler.getCode() == null && timeMultiplier < 60) {
            Thread.sleep(1000);
            timeMultiplier++;
        }
        String code = handler.getCode();
        String location = handler.getLocation();
        server.stop(0);
        executorService.shutdownNow();
        if(code == null || location == null) {
            throw new Exception("Unable to get code and location from server");
        }
        // TODO: update active DC
        return getCred(code);
    }

    private void refreshAccessToken() throws Exception {
        Url url = new Url(getDC());
        HttpUrl httpUrl = HttpUrl.parse(url.getAuthUrl())
                .newBuilder()
                .addPathSegments("oauth/v2/token")
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("refresh_token", cred.getRefreshToken())
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .post(formBody)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        ObjectMapper mapper = new ObjectMapper();
        PluginCredential refreshedCred = mapper.readValue(response.body().byteStream(), PluginCredential.class);
        response.close();
        if(refreshedCred.getAccessToken() == null) {
            throw new Exception("Unable to refresh access token");
        }
        cred.setCreatedTime(cred.getCreatedTime() == null ? System.currentTimeMillis() : cred.getCreatedTime());
        cred.setExpiresAt(System.currentTimeMillis() + (refreshedCred.getExpiredIn() * 1000));
        cred.setAccessToken(refreshedCred.getAccessToken());
        writeCredToFile(getConfigFile());
    }

    @Override
    public String getAccessToken(boolean forceRefresh) throws Exception {
        if(forceRefresh || (cred.getExpiresAt() < (System.currentTimeMillis() + MAX_EXPIRY))) {
            log.info("refreshing access token");
            refreshAccessToken();
        }
        log.info("returning access token");
        return cred.getAccessToken();
    }
}
