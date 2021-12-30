import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';
import '@vaadin/button';
import './addon-card';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import { searchStore } from './search-store';
import { appStore } from 'Frontend/stores/app-store';

@customElement('search-view')
export class SearchView extends View {
  constructor() {
    super();
    appStore.currentViewTitle = 'Search';
  }
  render() {
    return html`
      <div class="flex flex-col items-center">
        <h2 class="uppercase text-xs text-primary">Vaadin directory</h2>
        <h1>Add-on components and integrations</h1>
        <p>
          Search official and community add-ons and share your own to help
          others.
        </p>
        <p>
          Want to publish your work here? Great! <a href="javascript:window.haas.login()">Log in</a> and <a href="https://vaadin.com/directory/help">follow the instructions</a>.
        </p>
      </div>
      <h2>Featured Add-ons <i class="fa-solid fas fa-award"></i></h2>
      <div class="featured-list">
        ${([searchStore.featured[0]]).map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </div>
      <h2>Search for add-ons</h2>
      <vaadin-text-field
        style="min-width: 400px; max-width: 640px;"
        placeholder="Try e.g. 'upload' or 'icons'"
        .value=${searchStore.query}
        @change=${this.updateQuery}
        clear-button-visible>
      </vaadin-text-field>
      <div>Found total <b>1842</b> add-ons. <i class="text-2xs">Want to narrow down? Try filters like <a href="?q=v%3A8">v:8</a> or <a href="?q=author%3Ame">author:me</a></i></div>
      <div class="addons-grid" @filter-added=${this.filterAdded}>
        ${searchStore.addons.map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </div>

      <vaadin-button
        id="load-more-button"
        @click=${searchStore.fetchPage}
        ?disabled=${searchStore.loading}
        ?hidden=${searchStore.addons.length === 0}>
        Load more
      </vaadin-button>
    `;
  }

  firstUpdated() {
    searchStore.init();
    this.setupIntersectionObserver();
  }

  setupIntersectionObserver() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) searchStore.fetchPage();
      });
    });
    const button = this.renderRoot.querySelector('#load-more-button');
    if (button) {
      observer.observe(button);
    }
  }

  filterAdded({ filter }: FilterAddedEvent) {
    searchStore.addFilter(filter);
  }

  updateQuery(e: { target: HTMLInputElement }) {
    searchStore.setQuery(e.target.value);
  }
}
