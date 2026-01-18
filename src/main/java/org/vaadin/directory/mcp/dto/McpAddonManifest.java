package org.vaadin.directory.mcp.dto;

import java.time.LocalDate;
import java.util.List;

public class McpAddonManifest {

    private static final int SCHEMA_VERSION = 1;
    private static final String UNKNOWN_VALUE = "unknown";

    // Identity
    private String addonId = UNKNOWN_VALUE;
    private String name = UNKNOWN_VALUE;
    private String description = "";
    private List<String> tags = List.of();;

    // Compatibility
    private List<String> supportedVaadinVersions = List.of();
    private String compatibilityConfidence = UNKNOWN_VALUE;

    // Releases
    private String latestCompatibleVersion = UNKNOWN_VALUE;
    private LocalDate lastReleaseDate = LocalDate.MIN;

    // Install
    private McpInstallInfo install = new McpInstallInfo();

    // Usage
    private String docsUrl = UNKNOWN_VALUE;

    private String sourceRepoUrl = UNKNOWN_VALUE;

    private List<McpCodeSnippet> usageSnippets = List.of();

    private String license = UNKNOWN_VALUE;
    private double rating;

    private long ratingCount;

    public McpAddonManifest() {}

    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    public String getAddonId() {
        return addonId;
    }

    public void setAddonId(String addonId) {
        this.addonId = addonId != null ? addonId : UNKNOWN_VALUE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : UNKNOWN_VALUE;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : List.of();
    }

    public List<String> getSupportedVaadinVersions() {
        return supportedVaadinVersions;
    }

    public void setSupportedVaadinVersions(List<String> supportedVaadinVersions) {
        this.supportedVaadinVersions = supportedVaadinVersions != null ? supportedVaadinVersions : List.of();
    }

    public String getCompatibilityConfidence() {
        return compatibilityConfidence;
    }

    public void setCompatibilityConfidence(String compatibilityConfidence) {
        this.compatibilityConfidence = compatibilityConfidence != null ? compatibilityConfidence : UNKNOWN_VALUE;
    }

    public String getLatestCompatibleVersion() {
        return latestCompatibleVersion;
    }

    public void setLatestCompatibleVersion(String latestCompatibleVersion) {
        this.latestCompatibleVersion = latestCompatibleVersion != null ? latestCompatibleVersion : UNKNOWN_VALUE;
    }

    public LocalDate getLastReleaseDate() {
        return lastReleaseDate;
    }

    public void setLastReleaseDate(LocalDate lastReleaseDate) {
        this.lastReleaseDate = lastReleaseDate != null ? lastReleaseDate : LocalDate.MIN;
    }

    public McpInstallInfo getInstall() {
        return install;
    }

    public void setInstall(McpInstallInfo install) {
        this.install = install != null ? install : new McpInstallInfo();
    }

    public String getDocsUrl() {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl != null ? docsUrl : UNKNOWN_VALUE;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public void setSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl != null ? sourceRepoUrl : UNKNOWN_VALUE;
    }

    public List<McpCodeSnippet> getUsageSnippets() {
        return usageSnippets;
    }

    public void setUsageSnippets(List<McpCodeSnippet> usageSnippets) {
        this.usageSnippets = usageSnippets != null ? usageSnippets : List.of();
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license != null ? license : UNKNOWN_VALUE;
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
        private String mavenGroupId = UNKNOWN_VALUE;
        private String mavenArtifactId = UNKNOWN_VALUE;
        private String mavenVersion = UNKNOWN_VALUE;
        private String mavenSnippet = UNKNOWN_VALUE;
        private String gradleSnippet = UNKNOWN_VALUE;
        private String repository = UNKNOWN_VALUE;

        public McpInstallInfo() {}

        public String getMavenGroupId() {
            return mavenGroupId;
        }

        public void setMavenGroupId(String mavenGroupId) {
            this.mavenGroupId = mavenGroupId != null ? mavenGroupId : UNKNOWN_VALUE;
        }

        public String getMavenArtifactId() {
            return mavenArtifactId;
        }

        public void setMavenArtifactId(String mavenArtifactId) {
            this.mavenArtifactId = mavenArtifactId != null ? mavenArtifactId : UNKNOWN_VALUE;
        }

        public String getMavenVersion() {
            return mavenVersion;
        }

        public void setMavenVersion(String mavenVersion) {
            this.mavenVersion = mavenVersion != null ? mavenVersion : UNKNOWN_VALUE;
        }

        public String getMavenSnippet() {
            return mavenSnippet;
        }

        public void setMavenSnippet(String mavenSnippet) {
            this.mavenSnippet = mavenSnippet != null ? mavenSnippet : UNKNOWN_VALUE;
        }

        public String getGradleSnippet() {
            return gradleSnippet;
        }

        public void setGradleSnippet(String gradleSnippet) {
            this.gradleSnippet = gradleSnippet != null ? gradleSnippet : UNKNOWN_VALUE;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository != null ? repository : UNKNOWN_VALUE;
        }
    }

    public static class McpCodeSnippet {
        private String language = UNKNOWN_VALUE;
        private String code = "";
        private String description ="";

        public McpCodeSnippet() {}

        public McpCodeSnippet(String language, String code, String description) {
            this.language = language != null ? language : UNKNOWN_VALUE;
            this.code = code != null ? code : "";
            this.description = description != null ? description : "";
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language != null ? language : UNKNOWN_VALUE;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code != null ? code : "";
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description != null ? description : "";
        }
    }
}

