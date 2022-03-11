package org.vaadin.directory.endpoint.app;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.directory.BuildVersions;


@Endpoint
@AnonymousAllowed
public class AppEndpoint {

    private final BuildVersions buildVersions;

    public AppEndpoint(@Autowired BuildVersions buildVersions)   {
        this.buildVersions = buildVersions;
    }
    public @Nonnull VersionInfo getVersionInfo() {
        return new VersionInfo(buildVersions);
    }
}