package org.vaadin.directory.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import org.vaadin.directory.mcp.service.McpSearchService;
import org.vaadin.directory.mcp.service.McpAddonService;

/**
 * MCP Resources for exposing addon data via custom URI schemes.
 *
 * Resources provide read-only access to structured data through URI patterns.
 * They complement tools by offering direct data access without parameter complexity.
 */
@Component
public class AddonResources {

    private final McpSearchService searchService;
    private final McpAddonService addonService;
    private final ObjectMapper objectMapper;

    public AddonResources(McpSearchService searchService,
                         McpAddonService addonService) {
        this.searchService = searchService;
        this.addonService = addonService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Resource 1: Addon information by ID
     *
     * URI pattern: addon://details/{addonId}
     * Example: addon://details/avatar
     */
    @McpResource(
        uri = "addon://details/{addonId}",
        name = "Addon Details",
        description = "Get detailed addon information by ID (e.g., addon://details/avatar)",
        mimeType = "application/json"
    )
    public String addonDetails(String addonId) {
        try {
            var manifest = addonService.getAddonManifest(addonId, null);
            if (manifest == null) {
                return "{\"error\": \"Addon not found: " + addonId + "\"}";
            }
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(manifest);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Resource 2: Popular addons list
     *
     * URI pattern: addon://popular
     * Returns the top 10 most popular addons in the directory
     */
    @McpResource(
        uri = "addon://popular",
        name = "Popular Addons",
        description = "List of most popular Vaadin Directory addons",
        mimeType = "application/json"
    )
    public String popularAddons() {
        try {
            var results = searchService.search("", null, null, 10);
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(results);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
