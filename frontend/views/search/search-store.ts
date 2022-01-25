import { SearchEndpoint } from 'Frontend/generated/endpoints';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import SearchListResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchListResult';
import { autorun, makeAutoObservable } from 'mobx';
import { Filter } from './Filter';

class SearchStore {
  // State
  hasMore = true;
  loading = false;
  featured: SearchResult[] = [];
  addons: SearchResult[] = [];
  totalCount: number = -1;
  query = '';
  sort = 'recent';
  private page = 1;
  private pageSize = 12;

  // Init
  constructor() {
    makeAutoObservable(this, { init: false }, { autoBind: true });
  }

  init() {
    // Only init if we don't already have a state
    if (this.addons.length === 0) {
      this.fetchFeatured();
      this.readQueryFromURL();
      this.fetchPage();
    }
    if (this.totalCount < 0) {
      this.fetchTotalCount();
    }
  }

  // Server calls
  async fetchPage() {
    if (this.loading) return;
   this.setLoading(true);
    try {
      const res: SearchListResult = await SearchEndpoint.search(this.query, this.page, this.pageSize, this.sort, this.page == 1);
      if (this.page === 1 && res.totalCount) {
          this.setTotalCount(res.totalCount);
      }
      this.setHasMore(res.hasMore);
      this.setAddons(this.addons.concat(res.list));
      this.setPage(this.page + 1);
    } finally {
      this.setLoading(false);
    }
  }

  async fetchFeatured() {
    this.setFeatured(
      this.featured.concat(
        await SearchEndpoint.getFeaturedAddons()
      )
    );
  }

  async fetchTotalCount() {
    this.totalCount =
      await SearchEndpoint.searchCount(this.query);
}

  // Actions
  setLoading(loading: boolean) {
    this.loading = loading;
  }

  setFeatured(featured: SearchResult[]) {
    this.featured = featured;
  }

  setAddons(addons: SearchResult[]) {
    this.addons = addons;
  }

  setHasMore(hasMore: boolean) {
    this.hasMore = hasMore;
  }

  setPage(page: number) {
    this.page = page;
  }

  setTotalCount(totalCount: number) {
    this.totalCount = totalCount;
  }

  setQuery(query: string) {
    this.query = query;
    this.page = 1;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchPage();
  }

    setSort(sort: string) {
      this.sort = sort;
      this.page = 1;
      this.addons = [];
      this.writeQueryToURL();
      this.fetchPage();
    }


  addFilter(filter: Filter) {
    this.setQuery(
      `${this.query} ${filter.type}:${filter.value.replace(/\s/g, '_')}`
    );
  }

  // Utils
  readQueryFromURL() {
    const params = new URLSearchParams(location.search);
    const query = params.get('q') || '';

    if (query) {
      this.setQuery(query);
    }
  }

  writeQueryToURL() {
    const params = new URLSearchParams(location.search);
    params.set('q', this.query);

    if (this.query) {
      history.replaceState({}, '', `${location.pathname}?${params}${location.hash}`);
    } else {
      history.replaceState({}, '', `${location.pathname}${location.hash}`);
    }
  }
}

export const searchStore = new SearchStore();
