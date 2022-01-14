package org.vaadin.directory.endpoint.search;

import com.google.common.collect.Lists;
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
import org.vaadin.directory.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Endpoint
@AnonymousAllowed
public class SearchEndpoint {

    private final ComponentFrameworkVersionRepository frameworkVersionRepository;
    private final ComponentFramework polymer1, polymer2;
    private final ComponentFramework gwt1, gwt2;
    private final ComponentFramework vaadin6, vaadin7, vaadin8, vaadin10plus;
    private final List<ComponentFramework> vaadinMajorVersions;
    private final List<List<ComponentFrameworkVersion>> vaadinMinorVersions;
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
        this.vaadinMajorVersions = List.of(vaadin6,vaadin7,vaadin8,vaadin10plus);
        this.vaadinMinorVersions = this.vaadinMajorVersions.stream()
                .map(fw -> frameworkVersionRepository.findByFramework(fw))
                .collect(Collectors.toList());
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
    public @Nonnull SearchListResult search(
            String searchString, int page, int pageSize, String sort, boolean includeCount) {
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
            if (du != null) { owners = List.of(du); } else { return new SearchListResult(); };
        }

        // Resolve tag groups
        List<TagGroup> tagGroups = tagService.getTagGroups(qp.getTagGroups());

        // Framework
        ComponentFramework framework = null;  // All frameworks
        if (qp.getFramework() != null) {
            framework = frameworkRepository.findByName(qp.getFramework().getName());
            if (framework == null) { return new SearchListResult(); }
        }

        Set<ComponentFrameworkVersion> versions = Set.of();  // All versions
        if (framework != null) {
            if (qp.getFrameworkVersion() != null) {
                ComponentFrameworkVersion v = frameworkVersionRepository.findByFrameworkAndVersion(framework, qp.getFrameworkVersion());
                if (v != null) { versions = Set.of(v);
                } else { return new SearchListResult();}
            }
        } else if (qp.getFrameworkVersion() != null) {
            //TODO: Maybe instead try to match the first framework with the given version?
            return new SearchListResult();
        }

        SortFilter sortBy = SortFilter.fromString(sort).orElse(SortFilter.LAST_UPDATED);
        List<SearchResult> results = service
                .findAllComponentsBySearchCriteria(
                        List.of(Status.PUBLISHED),
                        qp.getKeywords(),
                        tagGroups,
                        owners,
                        sortBy,
                        framework,
                        versions,
                        PageRequest.of(page, pageSize))
                .stream()
                .map(c -> new SearchResult(c))
                .collect(Collectors.toList());

        Long count = null;
        if (includeCount) {
            if (results.size() < pageSize) {
                count = Long.valueOf(results.size());
            } else {
                count = service
                        .countAllComponentsBySearchCriteria(
                                List.of(Status.PUBLISHED),
                                qp.getKeywords(),
                                tagGroups,
                                owners,
                                framework,
                                versions);
            }
        }
        return new SearchListResult(results, count, results.size() == pageSize);
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

    @Transactional(readOnly = true)
    public @Nonnull Matrix getCompatibility(String urlIdentifier) {

        Optional<Component> maybveComponent = this.service.getComponentByUrl(urlIdentifier);
        if (!maybveComponent.isPresent()) { return new Matrix(List.of(),List.of(),List.of()); }

        Component component = maybveComponent.get();

        List<String> cols = new ArrayList<>();
        List<String> rows = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();

        // Collect all component versions
        List<ComponentVersion> versionList = component.getVersions().stream()
                .filter(ComponentVersion::getAvailable)
                .sorted(Comparator.comparing(ComponentVersion::getName))
                .collect(Collectors.toList());

        // Collect all available framework versions
        List<ComponentFrameworkVersion> frameworkVersions = new ArrayList<>();
        Lists.reverse(this.vaadinMinorVersions).stream().forEach(fw -> {
             fw.stream()
                     .filter(fwv -> !fwv.getVersion().endsWith("+"))
                     .sorted((v0,v1)-> {return Util.compareSemver(v0.getVersion(), v1.getVersion(), true);})
                     .forEach(fwv ->{
                 frameworkVersions.add(fwv);
             });
        });

        // Each row represents a framework version
        for (int i = 0; i < frameworkVersions.size(); i++) {
            ComponentFrameworkVersion fwv = frameworkVersions.get(i);
            ArrayList<String> dataRow = new ArrayList<>();
            rows.add(fwv.getVersion());
            data.add(dataRow);
        }

        // Each column represents a component version
        versionList.stream().forEach(version -> {
            cols.add(""+version.getName());
        });


        // Data is compatible vs. not-compatible matrix
        for (int r = 0; r < frameworkVersions.size(); r++) {
            ComponentFrameworkVersion fwv = frameworkVersions.get(r);
            List<String> row = data.get(r);
            for (int c = 0; c < versionList.size(); c++) {
                ComponentVersion version = versionList.get(c);
                List<String> names = version.getFrameworkVersions().stream().map(ComponentFrameworkVersion::getVersion).collect(Collectors.toList());
                boolean supported = Util.matchingVersionStrings(fwv.getVersion()).stream().anyMatch(name -> names.contains(name));
                row.add(supported ? "Y" :"");
            };
        }

        Matrix m = new Matrix(rows,cols,data);
        return m;
    }

    @Transactional(readOnly = true)
    public @Nonnull Matrix getVaadinCompatibility() {
        List<String> cols = new ArrayList<>();
        List<String> rows = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();

        ArrayList<String> row = new ArrayList<>();
        data.add(row);
        rows.add("Number of add-oms");
        this.vaadinMajorVersions.forEach(fw -> {
            List<ComponentFrameworkVersion> vl = this.frameworkVersionRepository.findByFramework(fw);
            if (fw == vaadin10plus) {
                vl.forEach(v -> {
                    AtomicInteger count = new AtomicInteger(0);
                    cols.add(v.getVersion());
                    row.add(""+v.getComponentVersions().size());
                });
            } else {
                AtomicInteger count = new AtomicInteger(0);
                cols.add(fw.getName());
                vl.forEach(v -> {
                    v.getComponentVersions().size();
                    count.addAndGet(v.getComponentVersions().size());
                });
                row.add(""+count.get());
            }

        });

        Matrix m = new Matrix(rows,cols,data);
        return m;
    }
}
