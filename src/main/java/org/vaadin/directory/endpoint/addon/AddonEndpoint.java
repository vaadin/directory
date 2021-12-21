package org.vaadin.directory.endpoint.addon;

import java.util.Optional;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Endpoint
@AnonymousAllowed
public class AddonEndpoint {

    private ComponentService service;

    AddonEndpoint(@Autowired ComponentService service) {
        this.service = service;
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String urlIdentifier) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        return maybeComponent.isPresent() ? new Addon(maybeComponent.get()) : null;
    }
}
