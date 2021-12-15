import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';
import '@vaadin/button';
import './components/addon-card';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { View } from './view';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import { SearchEndpoint } from 'Frontend/generated/endpoints';

@customElement('search-view')
export class SearchView extends View {
  @state() private searchString = '';
  @state() private addons: Addon[] = [];
  @state() loading = false;

  private page = 0;
  private pageSize = 10;

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
      <div class="flex justify-between">
        <vaadin-text-field placeholder="Search" @change=${this.updateSearch}>
        </vaadin-text-field>
      </div>
      <div class="addons-grid">
        ${this.addons.map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </div>

      <vaadin-button
        id="load-more-button"
        @click=${this.loadMore}
        ?disabled=${this.loading}>
        Load more
      </vaadin-button>
    `;
  }

  setupIntersectionObserver() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) this.loadMore();
      });
    });
    const button = this.renderRoot.querySelector('#load-more-button');
    if (button) {
      observer.observe(button);
    }
  }

  firstUpdated() {
    this.setupIntersectionObserver();
  }

  updateSearch(e: { target: HTMLInputElement }) {
    this.searchString = e.target.value;
    this.page = 0;
    this.addons = [];
  }

  async loadMore() {
    this.loading = true;
    this.addons = this.addons.concat(
      await SearchEndpoint.search(this.searchString, this.page++, this.pageSize)
    );
    this.loading = false;
  }

  async searchAddons() {
    // this.addons = await SearchEndpoint.search(this.searchString);
  }
}
