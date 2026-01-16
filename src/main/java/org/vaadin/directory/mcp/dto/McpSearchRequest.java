package org.vaadin.directory.mcp.dto;

import com.vaadin.hilla.Nonnull;

public class McpSearchRequest {

    @Nonnull
    private String query;

    private String vaadinVersion;

    private String type;

    private boolean maintainedOnly = true;

    private int limit = 10;

    public McpSearchRequest() {}

    public McpSearchRequest(String query, String vaadinVersion, String type, boolean maintainedOnly, int limit) {
        this.query = query;
        this.vaadinVersion = vaadinVersion;
        this.type = type;
        this.maintainedOnly = maintainedOnly;
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getVaadinVersion() {
        return vaadinVersion;
    }

    public void setVaadinVersion(String vaadinVersion) {
        this.vaadinVersion = vaadinVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMaintainedOnly() {
        return maintainedOnly;
    }

    public void setMaintainedOnly(boolean maintainedOnly) {
        this.maintainedOnly = maintainedOnly;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

