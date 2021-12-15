import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';
import '@vaadin/button';
import '../components/addon-card';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from '../components/filter-added-event';
import { searchStore } from './search-store';

@customElement('search-view')
export class SearchView extends View {
  render() {
    return html`
      <div class="flex flex-col items-center">
        <h2 class="uppercase text-xs text-primary">Vaadin directory</h2>
        <h1>Add-on components and integrations</h1>
        <p>
          Search official and community add-ons and share your own to help
          others.
        </p>
      </div>
      <h2>Browse All</h2>
      <vaadin-text-field
        placeholder="Search"
        .value=${searchStore.query}
        @change=${this.updateQuery}
        clear-button-visible>
      </vaadin-text-field>
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
