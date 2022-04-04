package org.vaadin.directory;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import com.vaadin.directory.backend.SortFilter;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.ComponentDirectoryUser;
import com.vaadin.directory.entity.directory.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AtomRSSFeedController {

    private String baseUrl;


    private ComponentService service;

    AtomRSSFeedController(@Value("${app.url}") String appUrl,
                          @Autowired ComponentService service){
        this.service = service;
        this.baseUrl = appUrl;
    }

    @RequestMapping(path = "/feed", method = RequestMethod.GET, produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public ResponseEntity<String> getFeed() {

        ComponentSyndFeed feed = new ComponentSyndFeed(null, this.service, this.baseUrl);
        feed.setAuthor(this.baseUrl);
        SyndFeedOutput output = new SyndFeedOutput();

        try {
            return ResponseEntity.ok(output.outputString(feed));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().body("Failed to create feed.");
    }

    private static class ComponentSyndFeed extends SyndFeedImpl {

        public static final String MIME_TYPE = "application/atom+xml";

        private final ComponentService componentService;
        private final ComponentDirectoryUser user;
        private final String baseUrl;

        public ComponentSyndFeed(ComponentDirectoryUser user, ComponentService componentService, String baseUrl) {
            this.componentService = componentService;
            this.user = user;
            this.baseUrl = baseUrl;
            setTitle("Add-ons | Vaadin Directory | Vaadin");
            setDescription(getTitle());
            setFeedType("atom_1.0");
            setEncoding("utf-8");
            setLink(baseUrl);
            setEntries(getAddonSyndEntries());
        }

        private List<SyndEntry> getAddonSyndEntries() {
            if (user != null) {
                // update feed title and set the user criteria on container
                setTitle(getTitle() + " of " + user.getName());
            }

            List<Component> addons = componentService.findAllComponentsBySearchCriteria(
                    Arrays.asList(Status.PUBLISHED, Status.REPORTED),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    SortFilter.LAST_UPDATED,
                    null,
                    Collections.emptySet(),
                    PageRequest.of(0, 50));

            return addons.stream()
                    .map(this::createEntry)
                    .collect(Collectors.toList());
        }

        private ComponentSyndEntry createEntry(Component c) {
            return new ComponentSyndEntry(c, baseUrl);
        }
    }

    private static class ComponentSyndEntry extends SyndEntryImpl {

        private final Component addon;

        public ComponentSyndEntry(Component component, String baseUrl) {
            this.addon = component;

            setTitle(component.getName());
            setDescription(getAddonDescription());
            setLink(baseUrl+ "addon/" + component.getUrlIdentifier());
            setPublishedDate(component.getModificationDate());
            setAuthor(component.getOwner().getName());
        }

        private SyndContent getAddonDescription() {
            SyndContent description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue(addon.getSummary());
            return description;
        }

    }
}
