package org.vaadin.directory.mcp.dto;

import com.vaadin.hilla.Nonnull;

public class McpAddonSummary {

    private static final int SCHEMA_VERSION = 1;

    @Nonnull
    private int schemaVersion = SCHEMA_VERSION;

    @Nonnull
    private String addonId;

    @Nonnull
    private String name;

    @Nonnull
    private String shortDescription;

    @Nonnull
    private String latestCompatibleVersion;

    @Nonnull
    private String compatibilityConfidence;

    @Nonnull
    private double rating;

    @Nonnull
    private long ratingCount;

    public McpAddonSummary() {}

    public McpAddonSummary(String addonId, String name, String shortDescription,
                          String latestCompatibleVersion, String compatibilityConfidence,
                          double rating, long ratingCount) {
        this.addonId = addonId;
        this.name = name;
        this.shortDescription = shortDescription;
        this.latestCompatibleVersion = latestCompatibleVersion != null ? latestCompatibleVersion : "unknown";
        this.compatibilityConfidence = compatibilityConfidence != null ? compatibilityConfidence : "unknown";
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getAddonId() {
        return addonId;
    }

    public void setAddonId(String addonId) {
        this.addonId = addonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLatestCompatibleVersion() {
        return latestCompatibleVersion;
    }

    public void setLatestCompatibleVersion(String latestCompatibleVersion) {
        this.latestCompatibleVersion = latestCompatibleVersion;
    }

    public String getCompatibilityConfidence() {
        return compatibilityConfidence;
    }

    public void setCompatibilityConfidence(String compatibilityConfidence) {
        this.compatibilityConfidence = compatibilityConfidence;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }
}

