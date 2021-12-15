package org.vaadin.directory.endpoint.search;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.directory.backend.SortFilter;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    private ComponentService service;

    public SearchEndpoint(@Autowired ComponentService service) {
        this.service = service;
    }

    public @Nonnull List<@Nonnull SearchResult> getAllAddons(int page,
            int pageSize) {
        return service.findAllPublishedComponents(PageRequest.of(page, pageSize)).stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }

    public @Nonnull List<@Nonnull SearchResult> search(
            String searchString, int page, int pageSize) {
        return service
                .findAllComponentsBySearchCriteria(
                        List.of(),
                        List.of(searchString),
                        List.of(),
                        List.of(),
                        SortFilter.LAST_UPDATED,
                        null,
                        Set.of(),
                        PageRequest.of(page, pageSize))
                .stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }


}
