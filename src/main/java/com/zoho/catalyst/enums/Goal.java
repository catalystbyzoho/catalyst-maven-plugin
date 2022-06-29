package com.zoho.catalyst.enums;

import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

public enum Goal {
    COPY_DEPENDENCY("copy-dependencies"),
    JAR("jar"),
    COMPILE("compile"),
    SINGLE("single");

    private final String value;

    Goal(String val) {
        value = goal(val);
    }

    public String getValue() {
        return value;
    }
}
