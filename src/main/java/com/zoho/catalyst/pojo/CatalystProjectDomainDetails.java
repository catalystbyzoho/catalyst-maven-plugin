package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalystProjectDomainDetails {
    @JsonProperty("project_domain_id")
    private String projectDomainId;
    @JsonProperty("project_domain_name")
    private String projectDomainName;
    @JsonProperty("project_domain")
    private String projectDomain;
}
