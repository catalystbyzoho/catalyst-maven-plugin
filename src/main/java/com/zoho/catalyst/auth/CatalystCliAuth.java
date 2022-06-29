package com.zoho.catalyst.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.catalyst.pojo.CatalystCliConfig;
import com.zoho.catalyst.pojo.PluginCredential;
import com.zoho.catalyst.utils.AuthUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class CatalystCliAuth extends Authenticator {
    private static final String CONFIG_FILE_NAME = "zcatalyst-cli.json";
    private static final String APP_NAME = "zcatalyst-cli-nodejs";

    private PluginCredential cred;

    protected CatalystCliAuth() throws Exception {
        File configFile = Paths.get(AuthUtil.getOSConfigDir(APP_NAME).toString(), CONFIG_FILE_NAME).toFile();
        if(!configFile.exists()) {
            throw new Exception("unable to find cli config file");
        }

        String credString = null;
        try(InputStream stream = new FileInputStream(configFile)) {
            ObjectMapper mapper = new ObjectMapper();
            CatalystCliConfig cliConfig = mapper.readValue(stream, CatalystCliConfig.class);
            String encryptedCred = cliConfig.getDcConfig().getCredential();
            credString = Crypt.decrypt(encryptedCred);
        }
        ObjectMapper mapper = new ObjectMapper();
        cred = mapper.readValue(credString.getBytes(StandardCharsets.UTF_8), PluginCredential.class);
    }

    @Override
    public String getAccessToken(boolean forceRefresh) {
        return cred.getAccessToken();
    }
}
