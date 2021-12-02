package org.vaadin.directory.search;

import java.util.List;
import javax.validation.constraints.NotBlank;
import com.vaadin.fusion.Nonnull;

public class Addon {

    private @Nonnull String slug;
    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String description;

    private @Nonnull List<@Nonnull String> tags;

    private @Nonnull String author;

    private double rating;

    public Addon(String name, String description) {
        this.slug = "addon-name";
        this.name = name;
        this.description = description;
        this.author = "Sami";
        rating = 4.3;
        this.tags = List.of("UI", "V14");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public String getSlug() {
        return slug;
    }

}
