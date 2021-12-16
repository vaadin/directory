package org.vaadin.directory.endpoint.search;

import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotBlank;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.Util;

public class SearchResult {

    private @Nonnull String urlIdentifier;

    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String summary;

    @NotBlank
    @Nonnull
    private LocalDate lastUpdated;

    private @Nonnull List<@Nonnull String> tags;

    private @Nonnull String author;

    private @Nonnull Double rating;

    public SearchResult() {}

    public SearchResult(Component component) {
        this.urlIdentifier = component.getUrlIdentifier();
        this.name = component.getDisplayName();
        this.summary = component.getSummary();
        this.lastUpdated = Util.dateToLocalDate(component.getLatestPublicationDate());
        this.author = "User " + component.getOwner().getId().toString();
        this.rating = component.getAverageRating() == null ? 0.0 : component.getAverageRating();
        this.tags = Util.tagsToStrings(component.getTagGroups());
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getAuthor() {
        return author;
    }

    public double getRating() {
        return rating;
    }

    public String getUrlIdentifier() {
        return urlIdentifier;
    }

}