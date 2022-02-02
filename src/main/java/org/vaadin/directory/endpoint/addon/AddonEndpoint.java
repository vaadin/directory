package org.vaadin.directory.endpoint.addon;

import java.util.Optional;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.backend.service.UserInfoService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Endpoint
@AnonymousAllowed
public class AddonEndpoint {

    private final UserInfoService userNameService;
    private final ComponentService service;

    AddonEndpoint(@Autowired ComponentService service,
                  @Autowired UserInfoService userNameService) {
        this.service = service;
        this.userNameService = userNameService;
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String urlIdentifier) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        return maybeComponent.isPresent() ? createAddon(maybeComponent.get()) : null;
    }

    private Addon createAddon(Component c) {
        Addon a = new Addon(c);
        a.setAuthor(userNameService.getNameforId(c.getOwner().getId()));
        return a;
    }

}
