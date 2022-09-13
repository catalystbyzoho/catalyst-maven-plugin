package com.zoho.catalyst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zoho.catalyst.Interceptor.CatalystAuthorizationInterceptor;
import com.zoho.catalyst.Interceptor.CatalystHttpInterceptor;
import com.zoho.catalyst.pojo.CatalystConfig;
import com.zoho.catalyst.pojo.CatalystProjectDetails;
import com.zoho.catalyst.pojo.PluginCredential;
import com.zoho.catalyst.utils.*;
import lombok.extern.java.Log;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Goal which forms zip file that can be uploaded to catalyst console
 */
@Log
@Mojo(name = "serve", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ServeMojo extends CatalystMojo {
    /**
     * port number to start the server
     *
     * @parameter property="port"
     * @readonly
     * @required
     */
    @Parameter(property = "port", defaultValue = "3010", readonly = true, required = true)
    protected int port;
    /**
     * Start server in debug mode
     *
     * @parameter property="debug"
     * @readonly
     * @required
     */
    @Parameter(property = "debug", defaultValue = "-1", readonly = true, required = true)
    protected int debug;

    @Override
    protected boolean authNeeded() {
        return true;
    }

    private HttpUrl getCatalystProjectsUrl(String baseUrl) {
        return HttpUrl.parse(baseUrl)
                .newBuilder()
                .addPathSegments("baas/v1/project/")
                .addPathSegment(project)
                .build();
    }

    @Override
    public void doExecute() throws Exception {
        Path catalystTargetDir = Paths.get(buildDir.getAbsolutePath(), ".build");
        Path fnDir = catalystTargetDir.resolve(mavenProject.getArtifactId());
        log.info("FN_DIR :: " + fnDir.toString());
        MojoUtil.Package packager = new MojoUtil.Package(getEnv());
        packager.compile();
        packager.copyDependency(buildDir);
        packager.createJar();
        File assemblyFile = packager.copyAssembly(buildDir);
        log.info("executing assembly file to target dir :: " + Paths.get(buildDir.getAbsolutePath()).relativize(fnDir).toString());
        packager.executeAssemble(assemblyFile, Paths.get(buildDir.getAbsolutePath()).relativize(fnDir).toString(), false);
        log.info("assembly execution complete");
        // read catalyst config file
        CatalystConfig config;
        try (InputStream configStream = FileUtils.openInputStream(fnDir.resolve("catalyst-config.json").toFile())) {
            ObjectMapper mapper = new ObjectMapper();
            log.info("configStream read val");
            config = mapper.readValue(configStream, CatalystConfig.class);
        }
        log.info("config read done");
        if(config != null && !config.getDeployment().getType().equals("advancedio")) {
            log.info("Only advancedIO functions is supported!!!");
            return;
        }
        Path invokerClassPth = catalystTargetDir.resolve(Paths.get(".catalyst", "aioserver", "JavaaioServer.class"));
        Path aioJavaSrc = catalystTargetDir.resolve("aioserver");
        new ResourceUtil().transferResourceFromJar("invoker/aio", aioJavaSrc);
        // give proper java path
        MojoUtil.Serve server = new MojoUtil.Serve();
        server.ensureInvoker(aioJavaSrc.resolve("JavaaioServer.java"), invokerClassPth);
        log.info("ensureInvoker complete");
        try (Stream<Path> stream = Files.walk(invokerClassPth.getParent())) {
            stream.forEach(source -> {
                if(source.toFile().isDirectory()) {
                    return;
                }
                Path sourceWithoutLib = Paths.get(source.toString().replaceFirst("lib" + File.separator, ""));
                FileUtil.copy(source, fnDir.resolve(invokerClassPth.getParent().relativize(sourceWithoutLib)));
            });
        }
        Url url = new Url(authConfig.getDc());
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new CatalystHttpInterceptor())
                .addInterceptor(new CatalystAuthorizationInterceptor(authConfig))
                .build();
        Request request = new Request.Builder()
                .url(getCatalystProjectsUrl(url.getAdminUrl()))
                .build();
        Call call = client.newCall(request);
        log.info("Executing the request");
        Response response = call.execute();
        CatalystProjectDetails projectDetails = ResponseUtil.deserializeCatalystResponse(response, CatalystProjectDetails.class);
        log.info("projectDetails name :::::: " + projectDetails.getProjectName());
        response.close();

        List<String> javaCommand = new ArrayList<String>();
        javaCommand.add("java");
        javaCommand.add("-cp");
        javaCommand.add(fnDir.resolve("*").toString() + ProcessUtil.classPathSep + fnDir.toString() + File.separator);
        javaCommand.add("-DCATALYST_FUNCTION_TYPE=applogic");
        javaCommand.add("-DisDev=true");
        if(debug != -1) {
            log.info("you can attach your debugger at port : " + debug);
            javaCommand.add("-Xdebug");
            javaCommand.add("-Xrunjdwp:transport=dt_socket,address=" + debug + ",server=y,suspend=y");
        }
        javaCommand.add(invokerClassPth.getFileName().toString().replace(".class", ""));
        javaCommand.add(String.valueOf(port));

        Map<String, String> env = new HashMap<>();
        env.put("X_ZOHO_CATALYST_IS_LOCAL", "true");
        env.put("X_ZOHO_CATALYST_FUNCTION_LOADED", "true");
        env.put("X_ZOHO_CATALYST_CODE_LOCATION", fnDir.toString() + File.separator);
        env.put("X_ZOHO_CATALYST_ACCOUNTS_URL", url.getAuthUrl());
        env.put("X_ZOHO_CATALYST_CONSOLE_URL", url.getAdminUrl());
        Process javaFnExe = ProcessUtil.executeCommand(javaCommand, fnDir.toString(), env);
        javaFnExe.waitFor();
    }
}

