import { SearchEndpoint } from 'Frontend/generated/endpoints';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { autorun, makeAutoObservable } from 'mobx';
import { Filter } from './Filter';

class SearchStore {
  // State
  loading = false;
  featured: SearchResult[] = [];
  addons: SearchResult[] = [];
  query = '';
  private page = 1;
  private pageSize = 10;

  // Init
  constructor() {
    makeAutoObservable(this, { init: false }, { autoBind: true });
  }

  init() {
    // Only init if we don't already have a state
    if (this.addons.length === 0) {
      this.fetchFeatured();
      this.readQueryFromURL();

      if (!this.loading) {
        this.fetchPage();
      }
    }
  }

  // Server calls
  async fetchPage() {
    this.setLoading(true);
    this.setAddons(
      this.addons.concat(
        await SearchEndpoint.search(this.query, this.page, this.pageSize)
      )
    );
    this.setPage(this.page + 1);
    this.setLoading(false);
  }

  async fetchFeatured() {
    this.setFeatured(
      this.featured.concat(
        await SearchEndpoint.getFeaturedAddons()
      )
    );
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

  setPage(page: number) {
    this.page = page;
  }

  setQuery(query: string) {
    this.query = query;
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
      history.replaceState({}, '', `${location.pathname}?${params}`);
    } else {
      history.replaceState({}, '', `${location.pathname}`);
    }
  }
}

export const searchStore = new SearchStore();
