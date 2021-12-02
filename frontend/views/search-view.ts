import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/button';
import './components/addon-card';
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
      <div class="flex flex-col items-center">
        <h2>Vaadin directory</h2>
        <h1>Add-on components and integrations</h1>
        <p>
          Search official and community add-ons and share your own to help
          others.
        </p>
      </div>
      <h2>Browse All</h2>
      <div class="flex justify-between">
        <vaadin-text-field
          label="Search"
          @input=${(e: any) => (this.searchString = e.target.value)}>
        </vaadin-text-field>
      </div>
      <div class="addons-grid">
        ${this.filteredAddons.map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </div>
    `;
  }

  async searchAddons() {
    this.addons = this.filteredAddons;
    // this.addons = await SearchEndpoint.search(this.searchString);
  }

  async firstUpdated() {
    this.addons = await SearchEndpoint.getAllAddons();
  }
}
