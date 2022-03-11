package org.vaadin.directory.endpoint.app;

import org.vaadin.directory.BuildVersions;

import dev.hilla.Nonnull;
import javax.validation.constraints.NotBlank;

public class VersionInfo {


    @Nonnull
    @NotBlank
    private final String buildTime;

    @Nonnull
    @NotBlank
    private final String startTime;

    @Nonnull
    @NotBlank
    private String version;
    
    public VersionInfo(BuildVersions buildVersions) {
        this.version = ""+buildVersions.getVersion();
        this.buildTime = ""+buildVersions.getBuildTime();
        this.startTime  = ""+buildVersions.getStartTime();
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getVersion() {
        return version;
    }

    public String getStartTime() { return startTime; }

}
