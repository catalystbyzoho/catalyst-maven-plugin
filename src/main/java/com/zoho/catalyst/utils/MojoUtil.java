package com.zoho.catalyst.utils;

import com.zoho.catalyst.enums.ExternalPlugin;
import com.zoho.catalyst.enums.Goal;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Log
public class MojoUtil {
    public static class Serve {
        public void ensureInvoker(@NotNull Path sourceJavaPth, @NotNull Path targetClassPth) throws Exception {
            Files.createDirectories(targetClassPth.getParent());
            List<String> command = new ArrayList<String>();
            command.add("javac");
            command.add("-cp");
            command.add(Paths.get("lib", "*").toString() + ProcessUtil.classPathSep + ".");
            command.add("-g");
            command.add("-d");
            command.add(targetClassPth.getParent().toString());
            command.add(sourceJavaPth.toString());
            Process javacProcess = ProcessUtil.executeCommand(command, sourceJavaPth.getParent().toString(), null);
            javacProcess.waitFor();
            // copy lib folder to target
            Path srcPth = Paths.get(sourceJavaPth.getParent().toString(), "lib");
            Path destPth = Paths.get(targetClassPth.getParent().toString(), "lib");
            log.info("copying lib dir" + srcPth.toString() + " ::: " + destPth.toString());
            FileUtil.copy(srcPth, destPth);
        }
    }
    public static class Package {
        private final MojoExecutor.ExecutionEnvironment env;
        public Package(@NotNull MojoExecutor.ExecutionEnvironment env) {
            this.env = env;
        }

        /**
         * Compile all relevant source files
         * @throws Exception When executing MAVEN_COMPILE plugin
         */
        public void compile() throws Exception {
            PluginExecutor compileExe = ExternalPlugin.MAVEN_COMPILE.getExecutor(this.env);
            compileExe.execute(Goal.COMPILE);
        }

        /**
         * Copy all dependency jar to lib folder in build dir
         * @param buildDir Base directly where lib folder should be present
         * @throws Exception When executing COPY_DEPENDENCY plugin
         */
        public void copyDependency(@NotNull File buildDir) throws Exception {
            // Copy all dependency jar to lib folder in build dir
            PluginExecutor dependencyExe = ExternalPlugin.MAVEN_DEPENDENCY.getExecutor(this.env);
            String outLibDir = Paths.get(buildDir.getAbsolutePath(), "lib").toString();
            dependencyExe.execute(
                    Goal.COPY_DEPENDENCY,
                    "<outputDirectory>" + outLibDir + "</outputDirectory>\n" +
                            "<overWriteReleases>false</overWriteReleases>\n" +
                            "<overWriteSnapshots>false</overWriteSnapshots>\n" +
                            "<overWriteIfNewer>true</overWriteIfNewer>"
            );
        }

        /**
         * Form a jar with classes and dependencies
         * @throws Exception When executing MAVEN_JAR plugin
         */
        public void createJar() throws Exception {
            PluginExecutor jarExe = ExternalPlugin.MAVEN_JAR.getExecutor(this.env);
            jarExe.execute(
                    Goal.JAR,
                    "<archive>\n" +
                            "<manifest>\n" +
                            "<addClasspath>true</addClasspath>\n" +
                            "<classpathPrefix>lib/</classpathPrefix>\n" +
                            "</manifest>\n" +
                            "</archive>"
            );
        }

        /**
         * Copy assembly file to <b>buildDir</b>/catalyst-assembly/bin.xml
         * @param buildDir Target Directory
         * @return Destination assembly file
         * @throws IOException When copying assembly file
         */
        public File copyAssembly(@NotNull File buildDir) throws IOException {
            File assemblyDestination = Paths.get(buildDir.getAbsolutePath(), "catalyst-assembly", "bin.xml").toFile();
            try(InputStream assemblyInputStream = getClass().getResourceAsStream("/user-assembly-bin.xml")) {
                FileUtils.copyInputStreamToFile(assemblyInputStream, assemblyDestination);
            }
            return assemblyDestination;
        }

        /**
         * Assemble all file to zip or directory
         * @param assembly Assembly file
         * @param name Final name of the zip or directory
         * @param isZip Should we assemble it as a zip
         * @throws Exception When executing MAVEN_ASSEMBLY plugin
         */
        public void executeAssemble(@NotNull File assembly, @NotNull String name, boolean isZip) throws Exception {
            // Assemble all file to catalyst-archive.zip
            PluginExecutor assemblyExe = ExternalPlugin.MAVEN_ASSEMBLY.getExecutor(this.env);
            String format = isZip ? "zip" : "dir";
            assemblyExe.execute(
                    Goal.SINGLE,
                    "<descriptors>"+ assembly.getAbsolutePath() +"</descriptors>\n" +
                            "<appendAssemblyId>false</appendAssemblyId>\n" +
                            "<formats><format>"+ format +"</format></formats>\n" +
                            "<finalName>"+ name +"</finalName>\n"
            );
        }
    }
}
