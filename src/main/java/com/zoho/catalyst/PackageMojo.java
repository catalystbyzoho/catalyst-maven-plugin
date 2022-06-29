package com.zoho.catalyst;

import com.zoho.catalyst.enums.ExternalPlugin;
import com.zoho.catalyst.enums.Goal;
import com.zoho.catalyst.utils.PluginExecutor;
import lombok.extern.java.Log;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Goal which forms zip file that can be uploaded to catalyst console
 */
@Log
@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends CatalystMojo {
    /**
     * Source archive name
     *
     * @parameter property="archiveName"
     * @readonly
     * @required
     */
    @Parameter(property = "archiveName", defaultValue = "catalyst-archive", readonly = true, required = true)
    protected String archiveName;

    @Override
    public void doExecute() throws Exception {
        // compile all relevant source files
        PluginExecutor compileExe = ExternalPlugin.MAVEN_COMPILE.getExecutor(getEnv());
        compileExe.execute(Goal.COMPILE);

        // Copy all dependency jar to lib folder in build dir
        PluginExecutor dependencyExe = ExternalPlugin.MAVEN_DEPENDENCY.getExecutor(getEnv());
        String outLibDir = Paths.get(buildDir.getAbsolutePath(), "lib").toString();
        dependencyExe.execute(
                Goal.COPY_DEPENDENCY,
                "<outputDirectory>" + outLibDir + "</outputDirectory>\n" +
                        "<overWriteReleases>false</overWriteReleases>\n" +
                        "<overWriteSnapshots>false</overWriteSnapshots>\n" +
                        "<overWriteIfNewer>true</overWriteIfNewer>"
        );

        // form a jar with classes and dependencies
        PluginExecutor jarExe = ExternalPlugin.MAVEN_JAR.getExecutor(getEnv());
        jarExe.execute(
                Goal.JAR,
                "<archive>\n" +
                        "<manifest>\n" +
                        "<addClasspath>true</addClasspath>\n" +
                        "<classpathPrefix>lib/</classpathPrefix>\n" +
                        "</manifest>\n" +
                        "</archive>"
        );

        // copy assembly file to target/catalyst-assembly/bin.xml
        File assemblyDest = Paths.get(buildDir.getAbsolutePath(), "catalyst-assembly", "bin.xml").toFile();
        try(InputStream assemblyInputStream = getClass().getResourceAsStream("/user-assembly-bin.xml")) {
            FileUtils.copyInputStreamToFile(assemblyInputStream, assemblyDest);
        }

        // Assemble all file to catalyst-archive.zip
        PluginExecutor assemblyExe = ExternalPlugin.MAVEN_ASSEMBLY.getExecutor(getEnv());
        assemblyExe.execute(
            Goal.SINGLE,
            "<descriptors>"+ assemblyDest.getAbsolutePath() +"</descriptors>\n" +
                "<appendAssemblyId>false</appendAssemblyId>\n" +
                "<finalName>"+archiveName.replace(".zip", "")+"</finalName>"
        );
    }
}
