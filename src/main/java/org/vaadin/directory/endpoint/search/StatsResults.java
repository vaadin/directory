package org.vaadin.directory.endpoint.search;

import com.vaadin.directory.entity.directory.Component;
import com.vaadin.hilla.Nonnull;
import jakarta.validation.constraints.NotBlank;

public class StatsResults {

    @NotBlank
    @Nonnull
    private  String urlIdentifier;

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

    @NotBlank
    @Nonnull
    private  Double avgRating;

    @NotBlank
    @Nonnull
    private Long totalVisits;

    @NotBlank
    @Nonnull
    private Long totalInstalls;

    @NotBlank
    @Nonnull
    private Long mavenDownloads;

    public StatsResults(Component c) {
        // assign fields
        this.urlIdentifier = c.getUrlIdentifier();
        this.name = c.getDisplayName();
        this.icon = c.getIcon() != null ?
                (c.getIcon().getLocalFileName()) :
                "defaultIconUrl";
        this.summary = c.getSummary();
        this.ratingCount = c.getRatingCount() == null ? 0 : c.getRatingCount();
        this.avgRating = c.getAverageRating() == null ? 0.0 : c.getAverageRating();
        this.totalVisits = 0L; // TODO: now fetched separately
        this.totalInstalls = 0L; // TODO: now fetched separately
        this.mavenDownloads = 0L; // TODO: now fetched separately
    }

    public String getUrlIdentifier() {
        return urlIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getSummary() {
        return summary;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public Long getTotalVisits() {
        return totalVisits;
    }

    public Long getTotalInstalls() {
        return totalInstalls;
    }

    public Long getMavenDownloads() {
        return mavenDownloads;
    }
}
