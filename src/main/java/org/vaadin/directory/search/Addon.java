package org.vaadin.directory.search;

import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotBlank;
import com.vaadin.fusion.Nonnull;

public class Addon {

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

    public Addon(String urlIdentifier,
                 String name,
                 String summary,
                 LocalDate lastUpdated,
                 Double rating,
                 String author,
                 List<String> tags) {
        this.urlIdentifier = urlIdentifier;
        this.name = name;
        this.summary = summary;
        this.lastUpdated = lastUpdated;
        this.author = "User "+author;
        this.rating = rating == null? 0 : rating;
        this.tags = tags;
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
