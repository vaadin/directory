import { SearchEndpoint } from 'Frontend/generated/endpoints';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import SearchListResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchListResult';
import { autorun, makeAutoObservable } from 'mobx';
import { Filter } from './Filter';

class SearchStore {
  // State
  isFirst = true;
  hasMore = true;
  loading = false;
  featured: string[] = [];
  addons: SearchResult[] = [];
  totalCount: number = -1;
  totalPages: number = -1;
  query = '';
  sort = 'recent';
  version = 'all'
  page = 1;
  pageSize = 12;
  currentUser = '';

  // Init
  constructor() {
    makeAutoObservable(this, { init: false }, { autoBind: true });
  }

  init() {
    // Only init if we don't already have a state
    if (this.addons.length === 0) {
      this.fetchFeatured();
      this.readQueryFromURL();
      this.readPageFromURL();
      this.fetchCurrentPage();
    }
    if (this.totalCount < 0) {
      this.fetchTotalCount();
    }
  }

  // Server calls
  async fetchPage(page: number) {
    if (this.loading) return;
   this.setLoading(true);
    try {
      const effectiveQuery = this.query + (this.version === 'all' ? '' : ' v:'+this.version);
      const res: SearchListResult = await SearchEndpoint.search(effectiveQuery , page, this.pageSize, this.sort, page == 1, this.currentUser);
      this.setHasMore(res.hasMore);
      this.setAddons(this.addons.concat(res.list));
      this.page = page;
    } finally {
      this.setLoading(false);
    }
  }

  async fetchFeatured() {
    const fts = await SearchEndpoint.getFeatured();
    this.setFeatured(fts);
  }

  async fetchTotalCount() {
    this.totalCount = await SearchEndpoint.searchCount(this.query);
    this.totalPages = Math.round(this.totalCount ? this.totalCount / this.pageSize : 0);

  }

  // Actions
  setLoading(loading: boolean) {
    this.loading = loading;
  }

  setFeatured(featured: string[]) {
    this.featured = featured ? featured : [];
  }

  setAddons(addons: SearchResult[]) {
    this.addons = addons;
  }

  setHasMore(hasMore: boolean) {
    this.hasMore = hasMore;
  }

  setPage(page: number) {
    this.page = page;
    this.isFirst = page === 1;
    this.writePageToURL();
    this.fetchCurrentPage();
  }

  setTotalCount(totalCount: number) {
    this.totalCount = totalCount;
  }

  setQuery(query: string) {
    this.query = query;
    this.page = 1;
    this.isFirst = true;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchCurrentPage();
  }

  setSort(sort: string) {
    this.sort = sort;
    this.page = 1;
    this.isFirst = true;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchCurrentPage();
  }

  setVersion(version: string) {
    this.version = version;
    this.page = 1;
    this.isFirst = true;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchCurrentPage();
  }

  setCurrentUser(user: string) {
    this.currentUser = user;
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
      this.query = query;
    }
  }

  readPageFromURL() {
    const params = new URLSearchParams(location.search);
    const page = params.get('page') || null;

    if (page) {
      this.page = +page || 1;
      this.isFirst = this.page === 1;
    }
  }

  writePageToURL() {
    const params = new URLSearchParams(location.search);
    params.set('page', this.page.toString());

    if (this.query) {
      history.replaceState({}, '', `${location.pathname}?${params}${location.hash}`);
    } else {
      history.replaceState({}, '', `${location.pathname}${location.hash}`);
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
