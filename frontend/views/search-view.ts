import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import { html } from 'lit';
import { router } from '../index';
import { views } from '../routes';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { Layout } from './view';
import { Binder, field } from '@vaadin/form';
import { getAllAddons, search } from 'Frontend/generated/SearchEndpoint';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import AddonModel from 'Frontend/generated/org/vaadin/directory/search/AddonModel';

@customElement('search-view')
export class SearchView extends Layout {

  @state()
  private searchString!:string;

  @state()
  private addons: Addon[] = [];

  render() {
    return html`
      <div style="padding: 25px">
        <div>
          <vaadin-text-field label="Search">
          </vaadin-text-field>
          <vaadin-button theme="primary" @click=${this.searchAddons}>Search</vaadin-button>
        </div>

        <h3>Addons</h3>
        <vaadin-grid .items="${this.addons}" theme="row-stripes" style="max-width: 400px"> <!--(8)-->
          <vaadin-grid-column path="name"></vaadin-grid-column>
          <vaadin-grid-column path="description"></vaadin-grid-column>
        </vaadin-grid>
      </div>
    `;
  }

  async searchAddons() {
    search(this.searchString).then((result) => { this.addons = result; })
  }

  async firstUpdated() {
    const addons = await getAllAddons();
    this.addons = addons;
  }
}