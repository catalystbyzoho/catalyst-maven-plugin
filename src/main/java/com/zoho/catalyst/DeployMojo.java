package com.zoho.catalyst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.catalyst.Interceptor.CatalystAuthorizationInterceptor;
import com.zoho.catalyst.Interceptor.CatalystHttpInterceptor;
import com.zoho.catalyst.pojo.CatalystConfig;
import com.zoho.catalyst.pojo.CatalystFunctionDetails;
import com.zoho.catalyst.utils.CatalystResponseDeserializer;
import com.zoho.catalyst.utils.ProcessUtil;
import com.zoho.catalyst.utils.ResponseUtil;
import com.zoho.catalyst.utils.Url;
import lombok.extern.java.Log;
import okhttp3.*;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Goal which deploys the formed zip file to console
 */
@Log
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends CatalystMojo {
    /**
     * Source archive name
     *
     * @parameter property="archiveName"
     * @readonly
     * @required
     */
    @Parameter(property = "archiveName", defaultValue = "catalyst-archive", readonly = true, required = true)
    protected String archiveName;

    /**
     * Total memory allocation
     *
     * @parameter property="memory"
     * @readonly
     */
    @Parameter(property = "memory", readonly = true)
    protected String memory;

    @Override
    protected boolean authNeeded() {
        return true;
    }

    private HttpUrl getCatalystApiUrl(String type) {
        Url url = new Url(authConfig.getDc());
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url.getAdminUrl())
                .newBuilder()
                .addPathSegments("baas/v1/project/")
                .addPathSegment(project);
        // TODO: move to enum
        switch (type) {
            case "basicio":
                httpBuilder.addPathSegments("function");
                break;
            case "advancedio":
                httpBuilder.addPathSegments("server");
                break;
        }
        httpBuilder.addPathSegments("upsert");
        return httpBuilder.build();
    }

    @Override
    public void doExecute() throws Exception {
        String sourceArchiveName = archiveName.replace(".zip", "") + ".zip";
        File catalystArchive = Paths.get(buildDir.getAbsolutePath(), sourceArchiveName).toFile();
        if(!catalystArchive.exists()) {
            log.info("Source archive '" + catalystArchive.toString() + "' does not exist!");
            if(!sourceArchiveName.equals("catalyst-archive.zip")) {
                throw new Exception("Given source archive not found");
            }
            List<String> packCommand = new ArrayList<>();
            packCommand.add("mvn" + (ProcessUtil.isWindows ? ".cmd" : ""));
            packCommand.add("catalyst:package");
            Process packageProcess = ProcessUtil.executeCommand(packCommand, mavenProject.getBasedir().getAbsolutePath(), null);
            packageProcess.waitFor();
        }

        // read catalyst config file
        CatalystConfig config = null;
        try (ZipFile zipFile = new ZipFile(catalystArchive)){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(entry.getName().equals("catalyst-config.json")) {
                    try(InputStream stream = zipFile.getInputStream(entry)) {
                        ObjectMapper mapper = new ObjectMapper();
                        config = mapper.readValue(stream, CatalystConfig.class);
                    }
                }
            }
        }
        if(config == null) {
            log.severe("unable to read config file");
            throw new Exception("catalyst-config.json cannot be read");
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new CatalystHttpInterceptor())
                .addInterceptor(new CatalystAuthorizationInterceptor(authConfig))
                .build();

        MultipartBody.Builder reqBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "code",
                        sourceArchiveName,
                        RequestBody.create(catalystArchive, MediaType.parse("application/octet-stream"))
                )
                .addFormDataPart("stack", config.getDeployment().getStack())
                .addFormDataPart("name", config.getDeployment().getName());
        if(config.getDeployment().getType().equals("basicio")) {
            reqBuilder.addFormDataPart("type", config.getDeployment().getType());
        }
        if(memory != null) {
            reqBuilder.addFormDataPart("memory", memory);
        }
        Request request = new Request.Builder()
                .url(getCatalystApiUrl(config.getDeployment().getType()))
                .addHeader("CATALYST-ORG", org)
                .put(reqBuilder.build())
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        CatalystFunctionDetails functionDetails = ResponseUtil.deserializeResponse(response, CatalystFunctionDetails.class, new CatalystResponseDeserializer<>(CatalystFunctionDetails.class));
        response.close();
        log.info("Successfully deployed");
        log.info("URL: " + functionDetails.getUrl());
    }
}
