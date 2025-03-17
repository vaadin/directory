import { SearchEndpoint } from 'Frontend/generated/endpoints';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import SearchListResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchListResult';
import { autorun, makeAutoObservable, action } from 'mobx';
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
  pageSize = 24;
  currentUser = '';

  abortController = null;

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
      this.fetchCurrentPage(true);
    }
    if (this.totalCount < 0) {
      //this.fetchTotalCount();
    }
  }


  fetchNextPage(includeCount: boolean) {
    this.fetchPage(this.page+1, includeCount);
  }


  fetchCurrentPage(includeCount: boolean) {
    this.fetchPage(this.page, includeCount);
  }

  // Page cache
   private pageCache = new Map<string, SearchListResult>();

  // Server calls
  async fetchPage(page: number, includeCount: boolean) {
    const effectiveQuery = this.query + (this.version === 'all' ? '' : ' v:'+this.version);
    const cacheKey = `${effectiveQuery}_${page}`;

    if (this.loading) {
        this.abortPageFetch();
    }

    if (this.pageCache.has(cacheKey)) {
      const cachedResult = this.pageCache.get(cacheKey)!;
      this.setHasMore(cachedResult.hasMore);
      this.totalCount = cachedResult.totalCount;
      this.totalPages = Math.round(this.totalCount ? this.totalCount / this.pageSize : 0);
      this.setAddons(this.addons.concat(cachedResult.list));
      action( e => this.page = page);
      return;
    }

    this.setLoading(true);
    try {
      const res: SearchListResult = await SearchEndpoint.search(effectiveQuery , page, this.pageSize, this.sort, includeCount, this.currentUser, { mute: true, signal: this.abortController.signal });
      this.pageCache.set(cacheKey, res);
      this.setHasMore(res.hasMore);
      if (res.totalCount) {
        // Update count if we ended up here with direct page link
        action( e => {
            this.totalCount = res.totalCount;
            this.totalPages = Math.round(this.totalCount ? this.totalCount / this.pageSize : 0);
            });
      }
      this.setAddons(this.addons.concat(res.list));
      action( e => this.page = page );
    } catch (ex) {
      console.log(""+ex);
    } finally {
      this.setLoading(false);
    }
  }

  async fetchFeatured() {
    const fts = await SearchEndpoint.getFeatured();
    this.setFeatured(fts);
  }

  async fetchTotalCount() {
    this.totalCount = await SearchEndpoint.searchCount(this.query, this.currentUser);
    this.totalPages = Math.round(this.totalCount ? this.totalCount / this.pageSize : 0);

    }

  abortPageFetch() {
    if (this.abortController) {
      this.abortController.abort("Page fetch aborted");
        this.abortController = null;
        console.log("aborted");
      }
  }

  // Actions
  setLoading(loading: boolean) {
    this.loading = loading;
      if (this.loading) {
          this.abortController = new AbortController();
      }
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
    this.fetchCurrentPage(true);
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
    this.fetchCurrentPage(true);
  }

  setSort(sort: string) {
    this.sort = sort;
    this.page = 1;
    this.isFirst = true;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchCurrentPage(true);
  }

  setVersion(version: string) {
    this.version = version;
    this.page = 1;
    this.isFirst = true;
    this.addons = [];
    this.writeQueryToURL();
    this.fetchCurrentPage(true);
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

  // Paging methods


  getPrevPageURL() {
    return this.getURL(this.query, this.page > 1 ?this.page-1 : -1);
  }

  getPageURL(page: number) {
    return this.getURL(this.query, page >= 1 && page <= this.totalPages ? page : -1);
  }

  getCurrentPageURL() {
    return this.getPageURL(this.page);
  }

  getNextPageURL() {
    return this.getURL(this.query, this.page < this.totalPages? this.page+1 : this.totalPages);
  }

  getURL(query: string = '', page:number = 1) {
    const params = new URLSearchParams(location.search);
    if (query.trim().length > 0) {
      params.set('q', query);
    }
    params.set('page', page > 0 ? page.toString(): '1');
    let path = `${location.pathname}?${params}${location.hash}`;
    return path;
  }
}

export const searchStore = new SearchStore();
