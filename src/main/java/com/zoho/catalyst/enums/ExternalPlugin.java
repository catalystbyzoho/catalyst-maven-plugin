package com.zoho.catalyst.enums;

import com.zoho.catalyst.utils.PluginExecutor;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;

import java.util.Arrays;
import java.util.EnumMap;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public enum ExternalPlugin {
    MAVEN_DEPENDENCY(
            "org.apache.maven.plugins",
            "maven-dependency-plugin",
            "3.2.0",
            new Goal[]{Goal.COPY_DEPENDENCY}
    ),
    MAVEN_JAR(
            "org.apache.maven.plugins",
            "maven-jar-plugin",
            "3.2.2",
            new Goal[]{Goal.JAR}
    ),
    MAVEN_COMPILE(
            "org.apache.maven.plugins",
            "maven-compiler-plugin",
            "3.8.0",
            new Goal[]{Goal.COMPILE}
    ),
    MAVEN_ASSEMBLY(
            "org.apache.maven.plugins",
            "maven-assembly-plugin",
            "3.3.0",
            new Goal[]{Goal.SINGLE}
    );

    private final String grp;
    private final String artifact;
    private final String pVersion;
    private final Plugin mojo;
    private final EnumMap<Goal, String> goals = new EnumMap<Goal, String>(Goal.class);

    ExternalPlugin(String gId, String aId, String v, Goal[] gl) {
        grp = groupId(gId);
        artifact = artifactId(aId);
        pVersion = version(v);
        mojo = plugin(grp, artifact, pVersion);
        // add goals to goals map
        Arrays.stream(gl).forEach(g -> goals.put(g, g.getValue()));
    }

    public Plugin getMojo() {
        return mojo;
    }

    public EnumMap<Goal, String> getGoals() {
        return goals;
    }

    public PluginExecutor getExecutor(MavenProject mavenProject, MavenSession mavenSession, BuildPluginManager pluginManager) {
        return new PluginExecutor(this, executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

    public PluginExecutor getExecutor(ExecutionEnvironment env) {
        return new PluginExecutor(this, env);
    }
}
