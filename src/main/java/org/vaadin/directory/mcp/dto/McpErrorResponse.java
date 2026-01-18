package org.vaadin.directory.mcp.dto;

/**
 * Error response for MCP tool invocations.
 */
public class McpErrorResponse {

    private String error;
    private String tool;

    public McpErrorResponse() {}

    public McpErrorResponse(String error, String tool) {
        this.error = error;
        this.tool = tool;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}

