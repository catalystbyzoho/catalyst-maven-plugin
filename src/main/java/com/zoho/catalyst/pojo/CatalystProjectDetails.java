package com.zoho.catalyst.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalystProjectDetails {
    @JsonProperty("id")
    private String id;
    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("project_domain_details")
    private CatalystProjectDomainDetails projectDomainDetails;
    @JsonProperty("env_details")
    private List<CatalystProjectEnvDetails> envDetails;
}
