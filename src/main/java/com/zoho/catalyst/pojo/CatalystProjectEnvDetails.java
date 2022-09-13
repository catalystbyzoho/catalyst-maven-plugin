package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalystProjectEnvDetails {
    @JsonProperty("id")
    private String id;
    @JsonProperty("env_name")
    private String envName;
    @JsonProperty("env_type")
    private String envType;
    @JsonProperty("env_zgid")
    private String envZgid;
}
