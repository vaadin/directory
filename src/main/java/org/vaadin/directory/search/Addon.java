package org.vaadin.directory.search;

import javax.validation.constraints.NotBlank;

public class Addon {

    @NotBlank
    private String name;

    @NotBlank
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