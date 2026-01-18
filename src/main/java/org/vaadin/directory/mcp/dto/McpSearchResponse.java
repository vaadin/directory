package org.vaadin.directory.mcp.dto;

import java.util.List;

public class McpSearchResponse {

    private static final int SCHEMA_VERSION = 1;
    private List<McpAddonSummary> addons;
    private long totalCount;
    private boolean hasMore;

    public McpSearchResponse() {}

    public McpSearchResponse(List<McpAddonSummary> addons, long totalCount, boolean hasMore) {
        this.addons = addons;
        this.totalCount = totalCount;
        this.hasMore = hasMore;
    }

    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    public List<McpAddonSummary> getAddons() {
        return addons;
    }

    public void setAddons(List<McpAddonSummary> addons) {
        this.addons = addons;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}

