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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
    @Cacheable(value = "cache1h", key = "'mcp-addon-' + #addonId + '-' + (#vaadinVersion != null ? #vaadinVersion : 'latest')")
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
        manifest.setTags(addon.getTags() != null ? addon.getTags() : List.of());

        // Find compatible versions
        List<AddonVersion> versions = addon.getVersions();
        if (versions == null) {
            versions = List.of();
        }
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
            manifest.setLastReleaseDate(LocalDate.MIN);
            manifest.setLicense("unknown");
            manifest.setInstall(new McpAddonManifest.McpInstallInfo());
        }

        // Usage
        String docsUrl = findDocsUrl(addon.getLinks() != null ? addon.getLinks() : List.of());
        manifest.setDocsUrl(docsUrl != null ? docsUrl : "unknown");

        String sourceUrl = findSourceUrl(addon.getLinks() != null ? addon.getLinks() : List.of());
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

        // Try exact or dot-bounded prefix match first
        for (int i = versions.size() - 1; i >= 0; i--) {
            AddonVersion v = versions.get(i);
            for (String compat : v.getCompatibility()) {
                // Match exact version or dot-separated prefix (e.g., "24" with "24.1")
                if (compat.equals(vaadinVersion)
                        || compat.startsWith(vaadinVersion + ".")
                        || vaadinVersion.startsWith(compat + ".")) {
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
            // Check major version match with bounds checking
            String[] compatParts = compat.split("\\.");
            String[] targetParts = vaadinVersion.split("\\.");
            if (compatParts.length > 0 && targetParts.length > 0 &&
                compatParts[0].equals(targetParts[0])) {
                return "medium";
            }
        }

        return "low";
    }

    private McpAddonManifest.McpInstallInfo createInstallInfo(AddonVersion version) {
        McpAddonManifest.McpInstallInfo info = new McpAddonManifest.McpInstallInfo();

        if (version.getInstalls().containsKey("Maven")) {
            String mavenSnippet = version.getInstalls().get("Maven");
            if (mavenSnippet == null) {
                mavenSnippet = "";
            }
            info.setMavenSnippet(mavenSnippet);

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

            // Generate Gradle snippet with sanitized coordinates
            String gradleSnippet = String.format("implementation '%s:%s:%s'",
                sanitizeCoordinate(coords[0]),
                sanitizeCoordinate(coords[1]),
                sanitizeCoordinate(coords[2]));
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

    /**
     * Sanitize Maven coordinates to prevent injection in Gradle snippets
     */
    private String sanitizeCoordinate(String coord) {
        if (coord == null || coord.equals("unknown")) {
            return coord;
        }
        // Allow only alphanumeric, dots, hyphens, and underscores
        return coord.matches("^[a-zA-Z0-9._-]+$") ? coord : "unknown";
    }

    private String[] parseMavenCoordinates(String mavenSnippet) {
        String[] result = {"unknown", "unknown", "unknown"};
        if (mavenSnippet == null || mavenSnippet.isEmpty()) {
            return result;
        }

        try {
            DocumentBuilder builder = getDocumentBuilder();

            // Parse the XML snippet
            Document doc = builder.parse(new ByteArrayInputStream(mavenSnippet.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            // Extract groupId
            NodeList groupIdList = doc.getElementsByTagName("groupId");
            if (groupIdList.getLength() > 0) {
                result[0] = groupIdList.item(0).getTextContent().trim();
            }

            // Extract artifactId
            NodeList artifactIdList = doc.getElementsByTagName("artifactId");
            if (artifactIdList.getLength() > 0) {
                result[1] = artifactIdList.item(0).getTextContent().trim();
            }

            // Extract version
            NodeList versionList = doc.getElementsByTagName("version");
            if (versionList.getLength() > 0) {
                result[2] = versionList.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            // Return defaults on parse error
        }

        return result;
    }


    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        // Create a DocumentBuilder with secure settings
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Disable external entities for security
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder();
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

