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
@CrossOrigin(
    origins = {
        "https://api.anthropic.com",
        "https://api.openai.com",
        "https://claude.ai",
        "http://localhost:*",
        "http://127.0.0.1:*"
    },
    allowedHeaders = {"Content-Type", "Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST},
    maxAge = 3600
)
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
        // Type-safe extraction with validation
        String query = request.get("query") != null ?
            request.get("query").toString().trim() : "";

        // Limit query length to prevent DoS
        if (query.length() > 500) {
            query = query.substring(0, 500);
        }

        String vaadinVersion = request.get("vaadinVersion") != null ?
            request.get("vaadinVersion").toString() : null;

        // Validate version format (e.g., "24", "24.1", "24.10")
        if (vaadinVersion != null && !vaadinVersion.matches("^[0-9]{1,2}(\\.[0-9]{1,2})?$")) {
            vaadinVersion = null;
        }

        String type = request.get("type") != null ?
            request.get("type").toString() : null;

        // Type-safe integer extraction with validation
        int limit = 10;
        if (request.containsKey("limit")) {
            Object value = request.get("limit");
            if (value instanceof Number) {
                limit = ((Number) value).intValue();
            }
        }

        // Enforce limits
        if (limit < 1) limit = 1;
        if (limit > 50) limit = 50;

        return searchService.search(query, vaadinVersion, type, limit);
    }

    /**
     * Get detailed addon information
     */
    @PostMapping(value = "/addon", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public McpAddonManifest getAddon(@RequestBody Map<String, Object> request) {
        String addonId = request.get("addonId") != null ?
            request.get("addonId").toString() : null;
        String vaadinVersion = request.get("vaadinVersion") != null ?
            request.get("vaadinVersion").toString() : null;

        if (addonId == null || addonId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "addonId is required");
        }

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

