package com.zoho.catalyst;

import com.zoho.catalyst.utils.MojoUtil;
import lombok.extern.java.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

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
        MojoUtil.Package packager = new MojoUtil.Package(getEnv());
        packager.compile();
        packager.copyDependency(buildDir);
        packager.createJar();
        File assemblyFile = packager.copyAssembly(buildDir);
        packager.executeAssemble(assemblyFile, archiveName.replace(".zip", ""), true);
    }
}
