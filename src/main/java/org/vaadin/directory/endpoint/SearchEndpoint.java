package org.vaadin.directory.endpoint;

import java.util.List;
import java.util.stream.Collectors;
import com.vaadin.directory.backend.service.ComponentService;
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

    public SearchEndpoint(@Autowired ComponentService service) {
        this.service = service;
    }

    public @Nonnull List<@Nonnull Addon> getAllAddons(int page, int pageSize) {
        return service.findAllPublishedComponents(PageRequest.of(page, pageSize)).stream()
                .map(c -> AddonEndpoint.componentToAddon(c))
                .collect(Collectors.toList());
    }

    public @Nonnull List<@Nonnull Addon> search(String searchString) {
        return List.of();
    }

}
