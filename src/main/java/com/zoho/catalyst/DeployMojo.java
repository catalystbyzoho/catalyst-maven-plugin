package com.zoho.catalyst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.catalyst.Interceptor.CatalystAuthorizationInterceptor;
import com.zoho.catalyst.Interceptor.CatalystHttpInterceptor;
import com.zoho.catalyst.pojo.CatalystConfig;
import lombok.extern.java.Log;
import okhttp3.*;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Goal which deploys the formed zip file to console
 */
@Log
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends CatalystMojo {
    public static final String CATALYST_API_URL = "https://api.catalyst.zoho.com";
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

    private HttpUrl getCatalystApiUrl() {
        String baseUrl = CATALYST_API_URL.replace(".com", authConfig.getDc().getExt());
        return HttpUrl.parse(baseUrl)
                .newBuilder()
                .addPathSegments("baas/v1/project/")
                .addPathSegment(project)
                .addPathSegments("server/upsert")
                .build();
    }

    @Override
    public void doExecute() throws Exception {
        String sourceArchiveName = archiveName.replace(".zip", "") + ".zip";
        File catalystArchive = Paths.get(buildDir.getAbsolutePath(), sourceArchiveName).toFile();
        if(!catalystArchive.exists()) {
            // if file doesn't exist request the user to execute package goal
            log.info("this file will not be deployed");
            return;
        }

        // read catalyst config file
        CatalystConfig config = null;
        ZipFile zipFile = new ZipFile(catalystArchive);
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
        if(config == null) {
            log.info("unable to read config file");
            return;
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
//                .addFormDataPart("type", config.getDeployment().getType());
        log.info("config.getDeployment().getType() ::: " + config.getDeployment().getType());
        if(memory != null) {
            reqBuilder.addFormDataPart("memory", memory);
        }
        log.info("getCatalystApiUrl() ::: " + getCatalystApiUrl());
        Request request = new Request.Builder()
                .url(getCatalystApiUrl())
                .put(reqBuilder.build())
                .build();

        Call call = client.newCall(request);
        log.info("Executing the request");
        Response response = call.execute();
        log.info("Response from server is :::::: " + response.body().string());
    }
}
