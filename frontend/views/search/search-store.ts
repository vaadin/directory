import { SearchEndpoint } from 'Frontend/generated/endpoints';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { autorun, makeAutoObservable } from 'mobx';
import { Filter } from './Filter';

class SearchStore {
  // State
  loading = false;
  addons: SearchResult[] = [];
  query = '';
  private page = 0;
  private pageSize = 10;

  // Init
  constructor() {
    makeAutoObservable(this, { init: false }, { autoBind: true });
  }

  init() {
    // Only init if we don't already have a state
    if (this.addons.length === 0) {
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
        await SearchEndpoint.search(this.query, this.page++, this.pageSize)
      )
    );
    this.setLoading(false);
  }

  // Actions
  setLoading(loading: boolean) {
    this.loading = loading;
  }

  setAddons(addons: SearchResult[]) {
    this.addons = addons;
  }

  setQuery(query: string) {
    this.query = query;
    this.page = 0;
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
