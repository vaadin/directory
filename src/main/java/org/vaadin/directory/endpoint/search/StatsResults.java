package org.vaadin.directory.endpoint.search;

import com.vaadin.directory.entity.directory.Component;
import com.vaadin.hilla.Nonnull;
import jakarta.validation.constraints.NotBlank;
import org.vaadin.directory.endpoint.addon.Addon;
import org.vaadin.directory.store.Store;

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

    public StatsResults(Component c, Store store) {
        // assign fields
        this.urlIdentifier = c.getUrlIdentifier();
        this.name = c.getDisplayName();
        this.icon = c.getIcon() != null ?
                (c.getIcon().getLocalFileName()) :
                "defaultIconUrl";
        this.summary = c.getSummary();
        // Combine legacy database ratings with the ratings stored in Firestore, as in Addon/SearchResult
        long dbCount = c.getRatingCount() == null ? 0 : c.getRatingCount();
        this.ratingCount = dbCount + store.getRatingCount(this.urlIdentifier);
        this.avgRating = Addon.weightedAvg(c.getAverageRating() == null ? 0.0 : c.getAverageRating(),
                dbCount,
                store.getAverageRating(this.urlIdentifier),
                store.getRatingCount(this.urlIdentifier));
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
