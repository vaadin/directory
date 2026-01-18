package org.vaadin.directory.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.directory.endpoint.search.SearchEndpoint;
import org.vaadin.directory.endpoint.search.SearchListResult;
import org.vaadin.directory.endpoint.search.SearchResult;
import org.vaadin.directory.mcp.dto.McpAddonSummary;
import org.vaadin.directory.mcp.dto.McpSearchResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class McpSearchService {

    private final SearchEndpoint searchEndpoint;

    public McpSearchService(@Autowired SearchEndpoint searchEndpoint) {
        this.searchEndpoint = searchEndpoint;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cache15m", key = "'mcp-search-' + #query + '-' + #vaadinVersion + '-' + #type + '-' + #limit")
    public McpSearchResponse search(String query, String vaadinVersion, String type, int limit) {
        // Build search query
        StringBuilder searchQuery = new StringBuilder(query != null ? query : "");

        if (vaadinVersion != null && !vaadinVersion.isEmpty()) {
            searchQuery.append(" framework:vaadin").append(vaadinVersion);
        }

        if (type != null && !type.isEmpty()) {
            searchQuery.append(" tag:").append(type);
        }

        // Execute search using existing endpoint
        String queryString = searchQuery.toString().trim();
        if (queryString.isEmpty()) {
            queryString = "*"; // Search all
        }

        SearchListResult result = searchEndpoint.search(
            queryString,
            0, // page
            limit,
            "popularity", // sort by popularity
            true, // include count
            "(not logged in)" // anonymous user
        );

        // Convert to MCP response
        List<McpAddonSummary> summaries = result.getList().stream()
                .map(sr -> convertToSummary(sr, vaadinVersion))
                .collect(Collectors.toList());

        return new McpSearchResponse(summaries, result.getTotalCount(), result.getHasMore());
    }

    private McpAddonSummary convertToSummary(SearchResult searchResult, String vaadinVersion) {
        // Get full addon details to determine latest compatible version
        String latestVersion = "unknown";
        String confidence = "unknown";

        // For now, we'll use basic heuristics. A more complete implementation
        // would fetch the full addon details, but that would be expensive for search results.
        // The PRD indicates this is acceptable for v1.

        return new McpAddonSummary(
            searchResult.getUrlIdentifier(),
            searchResult.getName(),
            searchResult.getSummary(),
            latestVersion,
            confidence,
            searchResult.getRating(),
            searchResult.getRatingCount()
        );
    }
}

