package org.vaadin.directory.endpoint.search;

import com.vaadin.directory.backend.SortFilter;
import com.vaadin.directory.backend.repository.directory.ComponentFrameworkRepository;
import com.vaadin.directory.backend.repository.directory.ComponentFrameworkVersionRepository;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    private final ComponentFrameworkVersionRepository frameworkVersionRepository;
    private final ComponentFramework polymer1, polymer2;
    private final ComponentFramework gwt1, gwt2;
    private final ComponentFramework vaadin6, vaadin7, vaadin8, vaadin10plus;
    private ComponentFrameworkRepository frameworkRepository;
    private ComponentDirectoryUserService userService;
    private ComponentService service;
    private TagGroupService tagService;

    public SearchEndpoint(@Autowired ComponentService service,
                          @Autowired TagGroupService tagService,
                          @Autowired ComponentFrameworkRepository frameworkRepository,
                          @Autowired ComponentFrameworkVersionRepository frameworkVersionRepository,
                          @Autowired ComponentDirectoryUserService userService) {
        this.service = service;
        this.tagService = tagService;
        this.userService = userService;
        this.frameworkRepository = frameworkRepository;
        this.frameworkVersionRepository = frameworkVersionRepository;

        // Warm up
        polymer1 = frameworkRepository.findByName("Polymer 1");
        polymer2 = frameworkRepository.findByName("Polymer 2");
        gwt1 = frameworkRepository.findByName("GWT 1");
        gwt2 = frameworkRepository.findByName("GWT 2");
        vaadin6 = frameworkRepository.findByName("Vaadin 6");
        vaadin7 = frameworkRepository.findByName("Vaadin 7");
        vaadin8 = frameworkRepository.findByName("Vaadin 8");
        vaadin10plus = frameworkRepository.findByName("Vaadin platform");
    }

    public @Nonnull List<@Nonnull SearchResult> getAllAddons(int page,
            int pageSize) {
        return service.findAllPublishedComponents(PageRequest.of(page, pageSize)).stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public @Nonnull List<@Nonnull SearchResult> getFeaturedAddons() {
        List<TagGroup> tagGroups = tagService.getTagGroups(List.of("Featured"));
        //TODO: This is a alternative hack for development
        List<String> keywords = List.of("");
        if (tagGroups == null || tagGroups.isEmpty()) {
            keywords = List.of("fluent");
        }
        List<ComponentDirectoryUser> owners = List.of();
        ComponentFramework framework = null;
        Set<ComponentFrameworkVersion> versions = Set.of();
        return service
                .findAllComponentsBySearchCriteria(
                        List.of(Status.PUBLISHED),
                        keywords,
                        tagGroups,
                        owners,
                        SortFilter.LAST_UPDATED,
                        framework,
                        versions, 
                        PageRequest.ofSize(3))
                .stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public @Nonnull List<@Nonnull SearchResult> search(
            String searchString, int page, int pageSize) {
        QueryParser qp = QueryParser.parse(searchString);

        List<ComponentDirectoryUser> owners = List.of(); // All users
        if (qp.getAuthor() != null ) {
            // TODO: We need to get the user names somehow. Not in current DB model.
            String user = qp.getAuthor();
            if (qp.isAuthorMe()) {
                user = "User_16"; // TODO: We should get this from login
            }
            long id = user.startsWith("User_") ? Long.parseLong(user.substring(5)) : -1;
            ComponentDirectoryUser du = userService.findById(id);
            owners = du != null? List.of(du) : List.of();
        }

        // Resolve tag groups
        List<TagGroup> tagGroups = tagService.getTagGroups(qp.getTagGroups());

        // Framework
        ComponentFramework framework = null;  // All frameworks
        if (qp.getFramework() != null) {
            framework = frameworkRepository.findByName(qp.getFramework().getName());
        }

        Set<ComponentFrameworkVersion> versions = Set.of();  // All versions
        if (framework != null && qp.getFrameworkVersion() != null) {
            ComponentFrameworkVersion v = frameworkVersionRepository.findByFrameworkAndVersion(framework, qp.getFrameworkVersion());
            if (v != null)  { versions = Set.of(v); };
        }
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

    @Transactional(readOnly = true)
    public @Nonnull Long searchCount(String searchString) {
        return service.countAllComponentsBySearchCriteria(
                List.of(Status.PUBLISHED),
                List.of(),
                List.of(),
                List.of(),
                null,
                Set.of());
    }

}
