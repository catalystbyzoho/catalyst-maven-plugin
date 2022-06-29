package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.zoho.catalyst.enums.DC;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginCredential {
    private String token;
    @JsonAlias("created_time")
    private Long createdTime;
    @JsonAlias("refresh_token")
    private String refreshToken;
    @JsonAlias("expires_at")
    private Long expiresAt;
    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("api_domain")
    private String apiDomain;
    @JsonAlias("token_type")
    private String tokenType;
    @JsonAlias("expires_in")
    private Long expiredIn;
    private DC dc;
}
