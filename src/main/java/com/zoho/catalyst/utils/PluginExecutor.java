package com.zoho.catalyst.utils;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;

import com.zoho.catalyst.enums.ExternalPlugin;
import com.zoho.catalyst.enums.Goal;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import lombok.extern.java.Log;

@Log
public class PluginExecutor {
    private final ExternalPlugin externalPlugin;
    private final MojoExecutor.ExecutionEnvironment exeEnv;

    public PluginExecutor(ExternalPlugin externalPlugin, MojoExecutor.ExecutionEnvironment env) {
        this.externalPlugin = externalPlugin;
        exeEnv = env;
    }

    public void execute(Goal givenGoal) throws Exception {
        execute(givenGoal, "");
    }
    public void execute(Goal givenGoal, String config) throws Exception {
        Xpp3Dom domConfig = PluginUtil.xmlToConfig(config);
        execute(givenGoal, domConfig);
    }

    public void execute(Goal givenGoal, Xpp3Dom config) throws Exception {
        if (!externalPlugin.getGoals().containsKey(givenGoal)) {
            throw new Exception("No such goal associated with this plugin");
        }
        // log the execution
        Plugin mojo = externalPlugin.getMojo();
        log.info(StringUtils.repeat("- ", 40));
        log.info(new StringBuilder("Executing plugin -> ")
                .append(mojo.getGroupId())
                .append(":")
                .append(mojo.getArtifactId())
                .append(":")
                .append(mojo.getVersion())
                .append(":")
                .append(givenGoal.getValue())
                .toString());
        log.info(StringUtils.repeat("- ", 40));
        executeMojo(externalPlugin.getMojo(), externalPlugin.getGoals().get(givenGoal), config, exeEnv);
        log.info("Successfully executed plugin.");
    }
}
