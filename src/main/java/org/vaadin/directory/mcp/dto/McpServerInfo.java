package org.vaadin.directory.mcp.dto;

import java.util.List;

public class McpServerInfo {
    private String name;
    private String version;
    private String description;
    private String toolEndpoint;
    private McpCapabilities capabilities;
    private List<McpToolInfo> tools;

    public McpServerInfo() {}

    public McpServerInfo(String name, String version, String description) {
        this.name = name;
        this.version = version;
        this.description = description;
    }

    public McpServerInfo(String name, String version, String description, String toolEndpoint) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.toolEndpoint = toolEndpoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolEndpoint() {
        return toolEndpoint;
    }

    public void setToolEndpoint(String toolEndpoint) {
        this.toolEndpoint = toolEndpoint;
    }

    public McpCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(McpCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public List<McpToolInfo> getTools() {
        return tools;
    }

    public void setTools(List<McpToolInfo> tools) {
        this.tools = tools;
    }

    public static class McpCapabilities {
        private boolean tools;

        public McpCapabilities() {}

        public McpCapabilities(boolean tools) {
            this.tools = tools;
        }

        public boolean isTools() {
            return tools;
        }

        public void setTools(boolean tools) {
            this.tools = tools;
        }
    }
}

