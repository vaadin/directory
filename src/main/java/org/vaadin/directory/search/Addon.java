package org.vaadin.directory.search;

import javax.validation.constraints.NotBlank;
import com.vaadin.fusion.Nonnull;

public class Addon {

    @NotBlank
    @Nonnull
    private String name;

    @NotBlank
    @Nonnull
    private String description;

    public Addon(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
