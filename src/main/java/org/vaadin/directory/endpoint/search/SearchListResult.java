package org.vaadin.directory.endpoint.search;

import com.vaadin.hilla.Nonnull;

import java.util.List;

public class SearchListResult {

    private Long totalCount;

    private boolean hasMore;

    private @Nonnull List<@Nonnull SearchResult> list;

    public SearchListResult(List<SearchResult> results) {
        this.list = results;
        this.totalCount = 0L;
        this.hasMore = false;
    }

    public SearchListResult(List<SearchResult> results, Long totalCount, boolean hasMore) {
        this.list = results;
        this.totalCount = totalCount;
        this.hasMore = hasMore;
    }

    public SearchListResult() { this.totalCount = 0L;}

    public Long getTotalCount() { return totalCount; }

    public List<SearchResult> getList() { return list; }

    public boolean getHasMore() { return hasMore; }


}
