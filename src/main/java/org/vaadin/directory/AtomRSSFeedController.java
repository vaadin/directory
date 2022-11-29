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

    private UrlConfig urlConfig;


    private ComponentService service;

    AtomRSSFeedController(@Autowired UrlConfig urlConfig,
                          @Autowired ComponentService service){
        this.service = service;
        this.urlConfig = urlConfig;
    }

    @RequestMapping(path = "/feed", method = RequestMethod.GET, produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public ResponseEntity<String> getFeed() {

        ComponentSyndFeed feed = new ComponentSyndFeed(null, this.service, this.urlConfig);
        feed.setAuthor(this.urlConfig.getAppUrl());
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
        private final UrlConfig urlConfig;

        public ComponentSyndFeed(ComponentDirectoryUser user, ComponentService componentService, UrlConfig urlConfig) {
            this.componentService = componentService;
            this.user = user;
            this.urlConfig = urlConfig;
            setTitle("Add-ons | Vaadin Directory | Vaadin");
            setDescription(getTitle());
            setFeedType("atom_1.0");
            setEncoding("utf-8");
            setLink(this.urlConfig.getAppUrl());
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
            return new ComponentSyndEntry(c, this.urlConfig);
        }
    }

    private static class ComponentSyndEntry extends SyndEntryImpl {

        private final Component addon;

        public ComponentSyndEntry(Component component, UrlConfig urlConfig) {
            this.addon = component;

            setTitle(component.getName());
            setDescription(getAddonDescription());
            setLink(urlConfig.getAppUrl()+"addon/" + component.getUrlIdentifier());
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
