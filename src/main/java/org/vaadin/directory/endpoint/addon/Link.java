package org.vaadin.directory.endpoint.addon;

import dev.hilla.Nonnull;

import javax.validation.constraints.NotBlank;

public class Link {

    @NotBlank
    @Nonnull
    private String name;

    private String icon;

    @NotBlank
    @Nonnull
    private String href;

    public Link(@Nonnull String name,@Nonnull String href, String icon) {
        this.name = name;
        this.icon = icon;
        this.href = href;
    }
}
