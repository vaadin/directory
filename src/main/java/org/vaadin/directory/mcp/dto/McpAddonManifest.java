package org.vaadin.directory.mcp.dto;

import com.vaadin.hilla.Nonnull;
import java.time.LocalDate;
import java.util.List;

public class McpAddonManifest {

    private static final int SCHEMA_VERSION = 1;

    @Nonnull
    private int schemaVersion = SCHEMA_VERSION;

    // Identity
    @Nonnull
    private String addonId;

    @Nonnull
    private String name;

    @Nonnull
    private String description;

    @Nonnull
    private List<String> tags;

    // Compatibility
    @Nonnull
    private List<String> supportedVaadinVersions;

    @Nonnull
    private String compatibilityConfidence;

    // Releases
    @Nonnull
    private String latestCompatibleVersion;

    @Nonnull
    private LocalDate lastReleaseDate;

    // Install
    @Nonnull
    private McpInstallInfo install;

    // Usage
    @Nonnull
    private String docsUrl;

    @Nonnull
    private String sourceRepoUrl;

    @Nonnull
    private List<McpCodeSnippet> usageSnippets;

    // Signals
    @Nonnull
    private String license;

    @Nonnull
    private double rating;

    @Nonnull
    private long ratingCount;

    public McpAddonManifest() {}

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getSupportedVaadinVersions() {
        return supportedVaadinVersions;
    }

    public void setSupportedVaadinVersions(List<String> supportedVaadinVersions) {
        this.supportedVaadinVersions = supportedVaadinVersions;
    }

    public String getCompatibilityConfidence() {
        return compatibilityConfidence;
    }

    public void setCompatibilityConfidence(String compatibilityConfidence) {
        this.compatibilityConfidence = compatibilityConfidence;
    }

    public String getLatestCompatibleVersion() {
        return latestCompatibleVersion;
    }

    public void setLatestCompatibleVersion(String latestCompatibleVersion) {
        this.latestCompatibleVersion = latestCompatibleVersion;
    }

    public LocalDate getLastReleaseDate() {
        return lastReleaseDate;
    }

    public void setLastReleaseDate(LocalDate lastReleaseDate) {
        this.lastReleaseDate = lastReleaseDate;
    }

    public McpInstallInfo getInstall() {
        return install;
    }

    public void setInstall(McpInstallInfo install) {
        this.install = install;
    }

    public String getDocsUrl() {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public void setSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
    }

    public List<McpCodeSnippet> getUsageSnippets() {
        return usageSnippets;
    }

    public void setUsageSnippets(List<McpCodeSnippet> usageSnippets) {
        this.usageSnippets = usageSnippets;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
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

    public static class McpInstallInfo {
        @Nonnull
        private String mavenGroupId;

        @Nonnull
        private String mavenArtifactId;

        @Nonnull
        private String mavenVersion;

        @Nonnull
        private String mavenSnippet;

        @Nonnull
        private String gradleSnippet;

        @Nonnull
        private String repository;

        public McpInstallInfo() {}

        public String getMavenGroupId() {
            return mavenGroupId;
        }

        public void setMavenGroupId(String mavenGroupId) {
            this.mavenGroupId = mavenGroupId;
        }

        public String getMavenArtifactId() {
            return mavenArtifactId;
        }

        public void setMavenArtifactId(String mavenArtifactId) {
            this.mavenArtifactId = mavenArtifactId;
        }

        public String getMavenVersion() {
            return mavenVersion;
        }

        public void setMavenVersion(String mavenVersion) {
            this.mavenVersion = mavenVersion;
        }

        public String getMavenSnippet() {
            return mavenSnippet;
        }

        public void setMavenSnippet(String mavenSnippet) {
            this.mavenSnippet = mavenSnippet;
        }

        public String getGradleSnippet() {
            return gradleSnippet;
        }

        public void setGradleSnippet(String gradleSnippet) {
            this.gradleSnippet = gradleSnippet;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository;
        }
    }

    public static class McpCodeSnippet {
        @Nonnull
        private String language;

        @Nonnull
        private String code;

        @Nonnull
        private String description;

        public McpCodeSnippet() {}

        public McpCodeSnippet(String language, String code, String description) {
            this.language = language;
            this.code = code;
            this.description = description;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

