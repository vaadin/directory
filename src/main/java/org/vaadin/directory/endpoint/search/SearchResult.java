package org.vaadin.directory.endpoint.search;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.hilla.Nonnull;
import org.vaadin.directory.UrlConfig;
import org.vaadin.directory.Util;
import org.vaadin.directory.endpoint.addon.Addon;
import org.vaadin.directory.store.Store;

public class SearchResult {

    private @Nonnull String urlIdentifier;

    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String icon;

    @NotBlank
    @Nonnull
    private String summary;

    @NotBlank
    @Nonnull
    private Long ratingCount;

    private @Nonnull List<@Nonnull String> tags;

    private @Nonnull String author;

    private @Nonnull Double rating;

    public SearchResult() {}

    public SearchResult(Component component, UrlConfig urlConfig, Store store) {
        this.urlIdentifier = component.getUrlIdentifier();
        this.name = component.getDisplayName();
        this.icon = component.getIcon() != null ?
                (component.getIcon().getLocalFileName().startsWith("http") ?
                        component.getIcon().getLocalFileName() :
                        urlConfig.getImageBaseUrl() + component.getIcon().getLocalFileName()):
                urlConfig.getDefaultIconUrl();
        this.summary = component.getSummary();
        this.author = "User " + component.getOwner().getId().toString();
        this.rating = Addon.weightedAvg(component.getAverageRating() == null ? 0.0 : component.getAverageRating(),
                component.getRatingCount() == null ? 0 : component.getRatingCount(),
                store.getAverageRating(this.urlIdentifier),
                store.getRatingCount(this.urlIdentifier)
        );
        this.ratingCount = component.getRatingCount() == null ? 0 : component.getRatingCount() + store.getRatingCount(this.urlIdentifier);
        this.tags = Util.tagsToStrings(component.getTagGroups());
    }

    public String getName() {
        return name;
    }

    public String getIcon() { return icon; }

    public String getSummary() {
        return summary;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) { this.author = author; }

    public double getRating() {
        return rating;
    }

    public Long getRatingCount() { return ratingCount; }

    public String getUrlIdentifier() {
        return urlIdentifier;
    }

}
