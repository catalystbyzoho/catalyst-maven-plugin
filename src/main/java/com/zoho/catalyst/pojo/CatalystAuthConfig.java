package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.zoho.catalyst.enums.AuthType;
import com.zoho.catalyst.enums.DC;
import com.zoho.catalyst.enums.Environment;
import lombok.Setter;

@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CatalystAuthConfig {
    private String type;
    private String dc;
    private String env;

    public AuthType getType() {
        return AuthType.fromString(this.type);
    }

    public DC getDc() {
        return DC.fromString(this.dc);
    }

    public Environment getEnv() {
        return Environment.fromString(this.env);
    }
}


