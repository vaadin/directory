package org.vaadin.directory.endpoint.app;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.vaadin.directory.BuildVersions;


@Endpoint
@AnonymousAllowed
public class AppEndpoint {

    private final BuildVersions buildVersions;

    public AppEndpoint(@Autowired BuildVersions buildVersions)   {
        this.buildVersions = buildVersions;
    }

    @Cacheable("applicationVersion")
    public @Nonnull VersionInfo getVersionInfo() {
        return new VersionInfo(buildVersions);
    }
}