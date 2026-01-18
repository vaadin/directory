package org.vaadin.directory.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.vaadin.directory.mcp.dto.*;
import org.vaadin.directory.mcp.service.McpAddonService;
import org.vaadin.directory.mcp.service.McpSearchService;

import java.util.List;
import java.util.Map;

/**
 * REST controller providing MCP (Model Context Protocol) endpoints for Vaadin Directory.
 * Exposes search and addon metadata APIs for AI tools and assistants.
 */
@RestController
@RequestMapping("/mcp-service")
@CrossOrigin(
    origins = { "*"},
    allowedHeaders = {"Content-Type", "Authorization", "Accept"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
    maxAge = 3600
)
public class McpController {

    private final McpSearchService searchService;
    private final McpAddonService addonService;
    private final ObjectMapper objectMapper;

    public McpController(
            @Autowired McpSearchService searchService,
            @Autowired McpAddonService addonService,
            @Autowired ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.addonService = addonService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get MCP server metadata
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public McpServerInfo getServerInfo() {
        McpServerInfo info = new McpServerInfo(
            "vaadin-directory-mcp",
            "1.0.0",
            "Vaadin Directory MCP Server - Search and retrieve addon metadata",
            "./mcp"
        );

        info.setCapabilities(new McpServerInfo.McpCapabilities(true));

        // Define search & addon tools
        McpToolInfo searchTool = getSearchToolInfo();
        McpToolInfo addonTool = getAddonToolInfo();
        info.setTools(List.of(searchTool, addonTool));
        return info;
    }

    private static McpToolInfo getAddonToolInfo() {
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
        return addonTool;
    }

    private static McpToolInfo getSearchToolInfo() {
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
        return searchTool;
    }

    /**
     * Unified MCP endpoint with streaming support.
     * Handles search, details (addon), and health tools.
     */
    @PostMapping(
        value = "/mcp",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> mcp(@RequestBody McpRequest req) {
        Object result = switch (req.tool()) {
            case "directory_search" -> searchTool(req.params());
            case "directory_getAddon" -> addonTool(req.params());
            case "health" -> healthTool(req.params());
            default -> errorTool("Unknown tool: " + req.tool(), req.tool());
        };

        try {
            byte[] json = objectMapper.writeValueAsBytes(result);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(json);
        } catch (Exception e) {
            byte[] errorJson = ("{\"error\":\"Serialization failed: " + e.getMessage() + "\"}").getBytes();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(errorJson);
        }
    }

    /**
     * Search tool implementation
     */
    private Object searchTool(Map<String, Object> params) {
        try {
            String query = params.get("query") != null ?
                params.get("query").toString().trim() : "";

            // Limit query length to prevent DoS
            if (query.length() > 500) {
                query = query.substring(0, 500);
            }

            String vaadinVersion = params.get("vaadinVersion") != null ?
                params.get("vaadinVersion").toString() : null;

            // Validate version format (e.g., "24", "24.1", "24.10")
            if (vaadinVersion != null && !vaadinVersion.matches("^[0-9]{1,2}(\\.[0-9]{1,2})?$")) {
                vaadinVersion = null;
            }

            String type = params.get("type") != null ?
                params.get("type").toString() : null;

            // Type-safe integer extraction with validation
            int limit = 10;
            if (params.containsKey("limit")) {
                Object value = params.get("limit");
                if (value instanceof Number) {
                    limit = ((Number) value).intValue();
                }
            }

            // Enforce limits
            if (limit < 1) limit = 1;
            if (limit > 50) limit = 50;

            return searchService.search(query, vaadinVersion, type, limit);
        } catch (Exception e) {
            return new McpErrorResponse("Search failed: " + e.getMessage(), "directory_search");
        }
    }

    /**
     * Addon details tool implementation
     */
    private Object addonTool(Map<String, Object> params) {
        try {
            String addonId = params.get("addonId") != null ?
                params.get("addonId").toString() : null;
            String vaadinVersion = params.get("vaadinVersion") != null ?
                params.get("vaadinVersion").toString() : null;

            if (addonId == null || addonId.trim().isEmpty()) {
                return new McpErrorResponse("addonId is required", "directory_getAddon");
            }

            McpAddonManifest manifest = addonService.getAddonManifest(addonId, vaadinVersion);

            if (manifest == null) {
                return new McpErrorResponse("Addon not found: " + addonId, "directory_getAddon");
            }

            return manifest;
        } catch (Exception e) {
            return new McpErrorResponse("Addon retrieval failed: " + e.getMessage(), "directory_getAddon");
        }
    }

    /**
     * Health check tool implementation
     */
    private Object healthTool(Map<String, Object> params) {
        return Map.of(
            "status", "ok",
            "service", "vaadin-directory-mcp"
        );
    }

    /**
     * Error tool implementation
     */
    private Object errorTool(String message, String tool) {
        return new McpErrorResponse(message, tool);
    }

    /**
     * Legacy search endpoint (kept for backward compatibility)
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
     * Legacy addon endpoint (kept for backward compatibility)
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
     * Legacy health check endpoint (kept for backward compatibility)
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        return Map.of(
            "status", "ok",
            "service", "vaadin-directory-mcp"
        );
    }
}

