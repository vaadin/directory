package org.vaadin.directory.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.vaadin.directory.search.Addon;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    private ComponentService service;

    private List<Component> allComponents = new ArrayList<>();
    private List<Addon> allAddons = new ArrayList<>();

    public SearchEndpoint(@Autowired ComponentService service) {
        this.service = service;
        allComponents = service.findAllPublishedComponents(PageRequest.of(0, 10));
        allAddons = allComponents.stream()
                .map(c -> AddonEndpoint.componentToAddon(c))
                .collect(Collectors.toList());
    }

    public @Nonnull List<@Nonnull Addon> getAllAddons() {
        return allAddons;
    }

    public @Nonnull List<@Nonnull Addon> search(String searchString) {
        return allAddons;
    }

}
