package com.zoho.catalyst.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalystConfig {
    private DeploymentConfig deployment;
    private ExecutionConfig execution;

    @Getter
    @Setter
    public class DeploymentConfig {
        private String name;
        private String stack;
        private String type;
    }

    @Getter
    @Setter
    public class ExecutionConfig {
        private String main;
    }
}
