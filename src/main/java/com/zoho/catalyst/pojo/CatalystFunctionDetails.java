package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown=true)
public class CatalystFunctionDetails {
    private Long id;
    private String name;
    @JsonProperty(value = "invoke_url")
    private String url;
    private String stack;
    private String type;
}
