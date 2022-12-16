package com.zoho.catalyst;

import com.zoho.catalyst.pojo.CatalystAuthConfig;
import com.zoho.catalyst.utils.LoggerUtil;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

public abstract class CatalystMojo extends AbstractMojo {
    @Component
    protected BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject mavenProject;

    /**
     * Location of the build directory.
     *
     * @parameter property="buildDir"
     * @readonly
     * @required
     */
    @Parameter(property = "buildDir", defaultValue = "${project.build.directory}", readonly = true, required = true)
    protected File buildDir;

    /**
     * Authentication to use.
     *
     * @parameter property="auth"
     */
    @Parameter(property = "auth", alias = "auth")
    protected CatalystAuthConfig authConfig;

    /**
     * project to use.
     *
     * @parameter property="project"
     * @required
     */
    @Parameter(property = "project", required = true)
    protected String project;

    @Parameter(property = "org", required = true)
    protected String org;

    abstract void doExecute() throws Exception;
    protected boolean authNeeded() {
        return false;
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if(authNeeded()) {
                assert authConfig.getDc() != null;
            }
            doExecute();
        } catch (Exception e) {
            throw new MojoExecutionException("Exception occurred while executing plugin! ", e);
        }
    }

    @Override
    public void setLog(Log log) {
        super.setLog(LoggerUtil.init(log));
    }

    public MojoExecutor.ExecutionEnvironment getEnv() {
        return executionEnvironment(this.mavenProject, this.mavenSession, this.pluginManager);
    }
}
