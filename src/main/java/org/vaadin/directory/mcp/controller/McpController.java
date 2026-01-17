package org.vaadin.directory.mcp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.vaadin.directory.mcp.dto.McpAddonManifest;
import org.vaadin.directory.mcp.dto.McpSearchResponse;
import org.vaadin.directory.mcp.dto.McpServerInfo;
import org.vaadin.directory.mcp.dto.McpToolInfo;
import org.vaadin.directory.mcp.service.McpAddonService;
import org.vaadin.directory.mcp.service.McpSearchService;

import java.util.List;
import java.util.Map;

/**
 * REST controller providing MCP (Model Context Protocol) endpoints for Vaadin Directory.
 * Exposes search and addon metadata APIs for AI tools and assistants.
 */
@RestController
@RequestMapping("/mcp/directory")
@CrossOrigin(origins = "*") // Allow AI tools to access from any origin
public class McpController {

    private final McpSearchService searchService;
    private final McpAddonService addonService;

    public McpController(
            @Autowired McpSearchService searchService,
            @Autowired McpAddonService addonService) {
        this.searchService = searchService;
        this.addonService = addonService;
    }

    /**
     * Get MCP server metadata
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public McpServerInfo getServerInfo() {
        McpServerInfo info = new McpServerInfo(
            "vaadin-directory-mcp",
            "1.0.0",
            "Vaadin Directory MCP Server - Search and retrieve addon metadata"
        );

        info.setCapabilities(new McpServerInfo.McpCapabilities(true));

        // Define search tool
        McpToolInfo searchTool = new McpToolInfo();
        searchTool.setName("directory_search");
        searchTool.setDescription("Search Vaadin Directory for addons. Returns a list of addon summaries with compatibility and rating information.");
        searchTool.setInputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "query", Map.of(
                                "type", "string",
                                "description", "Search query (addon name, keywords, or tags)"
                            ),
                            "vaadinVersion", Map.of(
                                "type", "string",
                                "description", "Vaadin major version (e.g., '24', '23') - optional"
                            ),
                            "type", Map.of(
                                "type", "string",
                                "description", "Addon type filter: component, integration, theme, or tool - optional"
                            ),
                            "maintainedOnly", Map.of(
                                "type", "boolean",
                                "description", "Only return maintained addons (default: true)"
                            ),
                            "limit", Map.of(
                                "type", "integer",
                                "description", "Maximum number of results (default: 10, max: 50)"
                            )
                        ),
                        "required", new String[]{"query"}
        ));

        // Define getAddon tool
        McpToolInfo addonTool = new McpToolInfo();
        addonTool.setName("directory_getAddon");
        addonTool.setDescription("Get detailed information about a specific Vaadin Directory addon including installation instructions, compatibility, and usage examples.");
        addonTool.setInputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "addonId", Map.of(
                                "type", "string",
                                "description", "The addon URL identifier (e.g., 'vaadin-grid-pro', 'avatar')"
                            ),
                            "vaadinVersion", Map.of(
                                "type", "string",
                                "description", "Target Vaadin major version (e.g., '24', '23')"
                            )
                        ),
                        "required", new String[]{"addonId", "vaadinVersion"}
        ));

        info.setTools(List.of(searchTool, addonTool));

        return info;
    }

    /**
     * Search for addons in Vaadin Directory
     */
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public McpSearchResponse search(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String vaadinVersion = (String) request.get("vaadinVersion");
        String type = (String) request.get("type");
        boolean maintainedOnly = request.containsKey("maintainedOnly") ?
            (Boolean) request.get("maintainedOnly") : true;
        int limit = request.containsKey("limit") ?
            ((Number) request.get("limit")).intValue() : 10;

        // Enforce max limit
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;

        return searchService.search(query, vaadinVersion, type, maintainedOnly, limit);
    }

    /**
     * Get detailed addon information
     */
    @PostMapping(value = "/addon", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public McpAddonManifest getAddon(@RequestBody Map<String, Object> request) {
        String addonId = (String) request.get("addonId");
        String vaadinVersion = (String) request.get("vaadinVersion");

        McpAddonManifest manifest = addonService.getAddonManifest(addonId, vaadinVersion);

        if (manifest == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Addon not found: " + addonId);
        }

        return manifest;
    }

    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        return Map.of(
            "status", "ok",
            "service", "vaadin-directory-mcp"
        );
    }
}

