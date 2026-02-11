package org.vaadin.directory.mcp.service;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.vaadin.directory.mcp.dto.*;
import org.vaadin.directory.mcp.exception.McpToolException;

/**
 * MCP Tools service exposing Vaadin Directory search and addon retrieval tools.
 *
 * Tools are automatically registered with Spring AI MCP server via @McpTool annotation.
 */
@Service
public class McpToolsService {

    private final McpSearchService searchService;
    private final McpAddonService addonService;

    public McpToolsService(McpSearchService searchService, McpAddonService addonService) {
        this.searchService = searchService;
        this.addonService = addonService;
    }

    @McpTool(
        name = "directory_search",
        description = """
    Search Vaadin Directory for addon component. Returns a list of Vaadin addon components summaries with compatibility, popularity and rating information.
    Use this tool to find addons by name, keywords, or type. After finding relevant addons, use the 'directory_getAddon' tool to get detailed information about a specific addon,
    including installation instructions and usage examples.
    """
    )
    public McpSearchResponse search(
        @McpToolParam(description = "Search query (addon name, component name, keywords)", required = true)
        String query,

        @McpToolParam(description = "Vaadin major version (e.g., '25', '24', '14', '8') - optional", required = false)
        String vaadinVersion,

        @McpToolParam(description = "Addon type filter: component, integration, theme, or tool - optional", required = false)
        String type,

        @McpToolParam(description = "Maximum number of results (default: 10, max: 50)", required = false)
        Integer limit
    ) {
        // Input validation
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.length() > 500) {
            cleanQuery = cleanQuery.substring(0, 500);
        }

        if (vaadinVersion != null && !vaadinVersion.matches("^[0-9]{1,2}(\\.[0-9]{1,2})?$")) {
            vaadinVersion = null;
        }

        int searchLimit = (limit != null) ? limit : 10;
        if (searchLimit < 1) searchLimit = 1;
        if (searchLimit > 50) searchLimit = 50;

        try {
            return searchService.search(cleanQuery, vaadinVersion, type, searchLimit);
        } catch (Exception e) {
            throw new McpToolException("Search failed: " + e.getMessage(), e);
        }
    }

    @McpTool(
        name = "directory_getAddon",
        description = """
     Get detailed information about a specific Vaadin Directory addon.
     including Maven installation instructions, compatibility, and usage examples.
     Call this tool when you need detailed info about a specific addon after finding it via search.
     """
    )
    public McpAddonManifest getAddon(
        @McpToolParam(description = "The addon URL identifier (e.g., 'vaadin-grid-pro', 'avatar')")
        String addonId,

        @McpToolParam(description = "Target Vaadin major version (e.g., '25', '24', '14', '8') - optional", required = false)
        String vaadinVersion
    ) {
        if (addonId == null || addonId.trim().isEmpty()) {
            throw new McpToolException("addonId is required");
        }

        try {
            McpAddonManifest manifest = addonService.getAddonManifest(addonId, vaadinVersion);
            if (manifest == null) {
                throw new McpToolException("Addon not found: " + addonId);
            }
            return manifest;
        } catch (Exception e) {
            throw new McpToolException("Addon retrieval failed: " + e.getMessage(), e);
        }
    }

}
