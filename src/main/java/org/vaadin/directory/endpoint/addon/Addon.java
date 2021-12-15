package org.vaadin.directory.endpoint.addon;

import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import com.vaadin.directory.entity.directory.Component;
import com.vaadin.fusion.Nonnull;
import org.vaadin.directory.Util;

public class Addon {

    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String summary;

    @Nonnull
    private String description;

    @NotBlank
    @Nonnull
    private LocalDate lastUpdated;

    private @Nonnull String author;

    private @Nonnull Double rating;

    public Addon() {}

    public Addon(Component component) {
        this.name = component.getDisplayName();
        this.summary = component.getSummary();
        this.description = component.getDescription();
        this.lastUpdated = Util.dateToLocalDate(component.getLatestPublicationDate());
        this.author = "User " + component.getOwner().getId().toString();
        this.rating = component.getAverageRating() == null ? 0.0 : component.getAverageRating();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

}
