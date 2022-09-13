package com.zoho.catalyst.utils;

import com.zoho.catalyst.enums.DC;

public class Url {
    private static final String CATALYST_AUTH_URL = "https://accounts.zoho.com";
    private static final String CATALYST_ADMIN_URL = "https://api.catalyst.zoho.com";
    private static final String CATALYST_CONSOLE_URL = "https://console.catalyst.zoho.com";

    private final DC dc;

    public Url() {
        this.dc = DC.COM;
    }

    public Url(DC dc) {
        this.dc = dc;
    }

    public String getAuthUrl() {
        return Url.CATALYST_AUTH_URL.replace(".com", this.dc.getExt());
    }

    public String getAdminUrl() {
        return Url.CATALYST_ADMIN_URL.replace(".com", this.dc.getExt());
    }

    public String getConsoleUrl() {
        return Url.CATALYST_CONSOLE_URL.replace(".com", this.dc.getExt());
    }
}
