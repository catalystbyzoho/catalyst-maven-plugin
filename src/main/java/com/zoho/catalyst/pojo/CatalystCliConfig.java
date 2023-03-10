package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zoho.catalyst.enums.DC;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalystCliConfig {
    @JsonProperty("active_dc")
    private String activeDC;
    private DcConfig com;

    public DcConfig getDcConfig() {
        // TODO: will change once we support CLI auth
        if(activeDC.equals(DC.COM.getKey())) {
            return com;
        }
        return null;
    }

    @Getter
    @Setter
    public class DcConfig {
        private String credential;
    }
}
