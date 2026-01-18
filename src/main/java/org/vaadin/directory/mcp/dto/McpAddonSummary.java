package org.vaadin.directory.mcp.dto;


public class McpAddonSummary {

    private static final int SCHEMA_VERSION = 1;

    private int schemaVersion = SCHEMA_VERSION;
    private String addonId;
    private String name;
    private String shortDescription;
    private double rating;
    private long ratingCount;

    public McpAddonSummary() {}

    public McpAddonSummary(String addonId, String name, String shortDescription,
                          double rating, long ratingCount) {
        this.addonId = addonId;
        this.name = name;
        this.shortDescription = shortDescription;
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

