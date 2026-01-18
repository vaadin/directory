package org.vaadin.directory.mcp.dto;

import java.util.Map;

/**
 * Unified MCP request record for all tool invocations.
 *
 * @param tool The tool name to invoke (e.g., "search", "details", "health")
 * @param params The parameters map for the tool
 */
public record McpRequest(String tool, Map<String, Object> params) {

    public McpRequest {
        if (tool == null || tool.isBlank()) {
            throw new IllegalArgumentException("Tool name cannot be null or blank");
        }
        // Ensure params is never null
        params = params != null ? params : Map.of();
    }
}

