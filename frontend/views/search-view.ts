import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/button';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { View } from './view';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import { SearchEndpoint } from 'Frontend/generated/endpoints';

@customElement('search-view')
export class SearchView extends View {
  @state()
  private searchString = '';

  @state()
  private addons: Addon[] = [];

  get filteredAddons() {
    const filter = new RegExp(this.searchString, 'i');
    return this.addons.filter(
      (addon) => addon.name?.match(filter) || addon.description?.match(filter)
    );
  }

  render() {
    return html`
      <div class="flex flex-col p-m w-full">
        <div class="flex gap-s items-baseline">
          <vaadin-text-field label="Search"> </vaadin-text-field>
          <vaadin-button theme="primary" @click=${this.searchAddons}>
            Search
          </vaadin-button>
        </div>

        <h3>Addons</h3>
        <vaadin-grid .items="${this.filteredAddons}" theme="row-stripes">
          <vaadin-grid-column path="name"></vaadin-grid-column>
          <vaadin-grid-column path="description"></vaadin-grid-column>
        </vaadin-grid>
      </div>
    `;
  }

  async searchAddons() {
    this.addons = await SearchEndpoint.search(this.searchString);
  }

  async firstUpdated() {
    const addons = await SearchEndpoint.getAllAddons();
    this.addons = addons;
  }
}
