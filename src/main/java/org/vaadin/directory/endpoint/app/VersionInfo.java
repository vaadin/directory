package org.vaadin.directory.endpoint.app;

import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.BuildVersions;

import javax.validation.constraints.NotBlank;

public class VersionInfo {

    @Nonnull
    @NotBlank
    private final String buildTime;

    @Nonnull
    @NotBlank
    private String version;
    
    public VersionInfo(BuildVersions buildVersions) {
        this.version = ""+buildVersions.getVersion();
        this.buildTime = ""+buildVersions.getBuildTime();
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getVersion() {
        return version;
    }
}
