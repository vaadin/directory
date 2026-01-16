package org.vaadin.directory.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.directory.endpoint.addon.Addon;
import org.vaadin.directory.endpoint.addon.AddonEndpoint;
import org.vaadin.directory.endpoint.addon.AddonVersion;
import org.vaadin.directory.endpoint.addon.Link;
import org.vaadin.directory.mcp.dto.McpAddonManifest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class McpAddonService {

    private final AddonEndpoint addonEndpoint;

    public McpAddonService(@Autowired AddonEndpoint addonEndpoint) {
        this.addonEndpoint = addonEndpoint;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache1h", key = "'mcp-addon-' + #addonId + '-' + #vaadinVersion")
    public McpAddonManifest getAddonManifest(String addonId, String vaadinVersion) {
        Addon addon = addonEndpoint.getAddon(addonId, "(not logged in)");
        if (addon == null) {
            return null;
        }

        McpAddonManifest manifest = new McpAddonManifest();

        // Identity
        manifest.setAddonId(addon.getUrlIdentifier());
        manifest.setName(addon.getName());
        manifest.setDescription(addon.getDescription() != null ? addon.getDescription() : addon.getSummary());
        manifest.setTags(addon.getTags());

        // Find compatible versions
        List<AddonVersion> versions = addon.getVersions();
        List<String> allSupportedVersions = extractAllSupportedVersions(versions);
        manifest.setSupportedVaadinVersions(allSupportedVersions);

        // Find best matching version
        AddonVersion bestVersion = findBestMatchingVersion(versions, vaadinVersion);
        String confidence = calculateCompatibilityConfidence(bestVersion, vaadinVersion);
        manifest.setCompatibilityConfidence(confidence);

        if (bestVersion != null) {
            manifest.setLatestCompatibleVersion(bestVersion.getName());
            manifest.setLastReleaseDate(bestVersion.getDate());
            manifest.setLicense(bestVersion.getLicense() != null ? bestVersion.getLicense() : "unknown");

            // Install info
            McpAddonManifest.McpInstallInfo installInfo = createInstallInfo(bestVersion);
            manifest.setInstall(installInfo);
        } else {
            manifest.setLatestCompatibleVersion("unknown");
            manifest.setLastReleaseDate(null);
            manifest.setLicense("unknown");
            manifest.setInstall(new McpAddonManifest.McpInstallInfo());
        }

        // Usage
        String docsUrl = findDocsUrl(addon.getLinks());
        manifest.setDocsUrl(docsUrl != null ? docsUrl : "unknown");

        String sourceUrl = findSourceUrl(addon.getLinks());
        manifest.setSourceRepoUrl(sourceUrl != null ? sourceUrl : "unknown");

        // Create usage snippets from code samples
        List<McpAddonManifest.McpCodeSnippet> snippets = createUsageSnippets(addon, bestVersion);
        manifest.setUsageSnippets(snippets);

        // Signals
        manifest.setRating(addon.getRating());
        manifest.setRatingCount(addon.getRatingCount());

        return manifest;
    }

    private List<String> extractAllSupportedVersions(List<AddonVersion> versions) {
        return versions.stream()
                .flatMap(v -> v.getCompatibility().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private AddonVersion findBestMatchingVersion(List<AddonVersion> versions, String vaadinVersion) {
        if (vaadinVersion == null || vaadinVersion.isEmpty()) {
            // Return latest version
            return versions.isEmpty() ? null : versions.get(versions.size() - 1);
        }

        // Normalize version (e.g., "24" -> "24.", "24.5" -> "24.5")
        String normalizedTarget = vaadinVersion.contains(".") ? vaadinVersion : vaadinVersion + ".";

        // Try exact match first
        for (int i = versions.size() - 1; i >= 0; i--) {
            AddonVersion v = versions.get(i);
            for (String compat : v.getCompatibility()) {
                if (compat.startsWith(normalizedTarget) || normalizedTarget.startsWith(compat)) {
                    return v;
                }
            }
        }

        // Return latest if no match
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    private String calculateCompatibilityConfidence(AddonVersion version, String vaadinVersion) {
        if (version == null || vaadinVersion == null || vaadinVersion.isEmpty()) {
            return "unknown";
        }

        String normalizedTarget = vaadinVersion.contains(".") ? vaadinVersion : vaadinVersion + ".";

        for (String compat : version.getCompatibility()) {
            if (compat.equals(vaadinVersion) || compat.startsWith(normalizedTarget)) {
                return "high";
            }
            // Check major version match
            String compatMajor = compat.split("\\.")[0];
            String targetMajor = vaadinVersion.split("\\.")[0];
            if (compatMajor.equals(targetMajor)) {
                return "medium";
            }
        }

        return "low";
    }

    private McpAddonManifest.McpInstallInfo createInstallInfo(AddonVersion version) {
        McpAddonManifest.McpInstallInfo info = new McpAddonManifest.McpInstallInfo();

        if (version.getInstalls().containsKey("Maven")) {
            String mavenSnippet = version.getInstalls().get("Maven");
            info.setMavenSnippet(mavenSnippet != null ? mavenSnippet : "");

            // Parse Maven coordinates from snippet
            String[] coords = parseMavenCoordinates(mavenSnippet);
            info.setMavenGroupId(coords[0]);
            info.setMavenArtifactId(coords[1]);
            info.setMavenVersion(coords[2]);

            // Check for Vaadin repository requirement
            if (coords[0].startsWith("org.vaadin.") || coords[0].startsWith("com.vaadin.")) {
                info.setRepository("https://maven.vaadin.com/vaadin-addons");
            } else {
                info.setRepository("maven-central");
            }

            // Generate Gradle snippet
            String gradleSnippet = String.format("implementation '%s:%s:%s'", coords[0], coords[1], coords[2]);
            info.setGradleSnippet(gradleSnippet);
        } else {
            info.setMavenGroupId("unknown");
            info.setMavenArtifactId("unknown");
            info.setMavenVersion("unknown");
            info.setMavenSnippet("");
            info.setGradleSnippet("");
            info.setRepository("unknown");
        }

        return info;
    }

    private String[] parseMavenCoordinates(String mavenSnippet) {
        String[] result = {"unknown", "unknown", "unknown"};
        if (mavenSnippet == null) return result;

        try {
            // Extract groupId
            int groupIdStart = mavenSnippet.indexOf("<groupId>");
            int groupIdEnd = mavenSnippet.indexOf("</groupId>");
            if (groupIdStart != -1 && groupIdEnd != -1) {
                result[0] = mavenSnippet.substring(groupIdStart + 9, groupIdEnd).trim();
            }

            // Extract artifactId
            int artifactIdStart = mavenSnippet.indexOf("<artifactId>");
            int artifactIdEnd = mavenSnippet.indexOf("</artifactId>");
            if (artifactIdStart != -1 && artifactIdEnd != -1) {
                result[1] = mavenSnippet.substring(artifactIdStart + 12, artifactIdEnd).trim();
            }

            // Extract version
            int versionStart = mavenSnippet.indexOf("<version>");
            int versionEnd = mavenSnippet.indexOf("</version>");
            if (versionStart != -1 && versionEnd != -1) {
                result[2] = mavenSnippet.substring(versionStart + 9, versionEnd).trim();
            }
        } catch (Exception e) {
            // Return defaults on parse error
        }

        return result;
    }

    private String findDocsUrl(List<Link> links) {
        for (Link link : links) {
            String name = link.getName() != null ? link.getName().toLowerCase() : "";
            if (name.contains("doc") || name.contains("guide") || name.contains("readme")) {
                return link.getHref();
            }
        }
        return null;
    }

    private String findSourceUrl(List<Link> links) {
        for (Link link : links) {
            String href = link.getHref() != null ? link.getHref().toLowerCase() : "";
            if (href.contains("github.com") || href.contains("gitlab.com") || href.contains("bitbucket.org")) {
                return link.getHref();
            }
        }
        return null;
    }

    private List<McpAddonManifest.McpCodeSnippet> createUsageSnippets(Addon addon, AddonVersion version) {
        List<McpAddonManifest.McpCodeSnippet> snippets = new ArrayList<>();

        // Add code samples from addon if available
        if (addon.getCodeSamples() != null && !addon.getCodeSamples().isEmpty()) {
            for (var sample : addon.getCodeSamples()) {
                snippets.add(new McpAddonManifest.McpCodeSnippet(
                    "java",
                    sample.getCode(),
                    sample.getDescription()
                ));
            }
        }

        // If no code samples, generate a minimal import example
        if (snippets.isEmpty() && version != null && version.getInstalls().containsKey("Maven")) {
            String mavenSnippet = version.getInstalls().get("Maven");
            String[] coords = parseMavenCoordinates(mavenSnippet);
            if (!coords[0].equals("unknown")) {
                String packageName = coords[0];
                String simpleUsage = String.format(
                    "// Add to your Flow view:\nimport %s.*;\n\n// Usage example:\n// See documentation for specific component usage",
                    packageName
                );
                snippets.add(new McpAddonManifest.McpCodeSnippet(
                    "java",
                    simpleUsage,
                    "Basic import"
                ));
            }
        }

        return snippets;
    }
}

