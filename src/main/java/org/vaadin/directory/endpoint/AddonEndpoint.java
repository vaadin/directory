package org.vaadin.directory.endpoint;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.directory.backend.service.ComponentService;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.directory.entity.directory.Tag;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.fusion.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.directory.search.Addon;

@Endpoint
@AnonymousAllowed
public class AddonEndpoint {

    private ComponentService service;

    AddonEndpoint(@Autowired ComponentService service) {
        this.service = service;
    }

    public Addon getAddon(String urlIdentifier) {
        Optional<Component> maybeComponent = service.getComponentByUrl(urlIdentifier);
        return maybeComponent.isPresent() ? componentToAddon(maybeComponent.get()) : null;
    }

    public static Addon componentToAddon(Component c) {
        return new Addon(c.getUrlIdentifier(),
                c.getDisplayName(),
                c.getSummary(),
                dateToLocalDate(c.getLatestPublicationDate()),
                c.getAverageRating(),
                c.getOwner().getId().toString(),
                tagsToStrings(c.getTags()));
    }

    public static LocalDate dateToLocalDate(Date date) {
        if (date == null)
            return LocalDate.of(2000, 1, 1);
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static List<String> tagsToStrings(Set<Tag> tags) {
        if (tags == null) {
            return Collections.EMPTY_LIST;
        }
        return tags.stream()
                .map(t -> t.getName())
                .collect(Collectors.toList());
    }
}
