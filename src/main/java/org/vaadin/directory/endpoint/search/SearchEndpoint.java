package org.vaadin.directory.endpoint.search;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.directory.backend.SortFilter;
import com.vaadin.directory.backend.service.ComponentDirectoryUserService;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.backend.service.TagGroupService;
import com.vaadin.directory.entity.directory.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import com.vaadin.fusion.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    private ComponentDirectoryUserService userService;
    private ComponentService service;
    private TagGroupService tagService;

    public SearchEndpoint(@Autowired ComponentService service,
                          @Autowired TagGroupService tagService,
                          @Autowired ComponentDirectoryUserService userService) {
        this.service = service;
        this.tagService = tagService;
        this.userService = userService;
    }

    public @Nonnull List<@Nonnull SearchResult> getAllAddons(int page,
            int pageSize) {
        return service.findAllPublishedComponents(PageRequest.of(page, pageSize)).stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public @Nonnull List<@Nonnull SearchResult> search(
            String searchString, int page, int pageSize) {
        QueryParser qp = QueryParser.parse(searchString);

        List<ComponentDirectoryUser> owners = List.of(); // All users
        if (qp.getAuthor().isPresent()) {
            // TODO: We need to get the user names somehow. Not in current model.
            // userService.findByFirstNameAndLastName(qp.getAuthor().get(),"");
        }

        // Resolve tag groups
        List<TagGroup> tagGroups = tagService.getTagGroups(qp.getTagGroups());
        ComponentFramework framework = null;  // All frameworks
        Set<ComponentFrameworkVersion> versions = Set.of();  // All versions
        return service
                .findAllComponentsBySearchCriteria(
                        List.of(Status.PUBLISHED),
                        qp.getKeywords(),
                        tagGroups,
                        owners,
                        SortFilter.LAST_UPDATED,
                        framework,
                        versions,
                        PageRequest.of(page, pageSize))
                .stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }


}
