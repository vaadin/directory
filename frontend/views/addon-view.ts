import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { View } from './view';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';

@customElement('addon-view')
export class AddonView extends View {
  @state()
  private addon!: Addon;

  render() {
    return html`
        <div>
            <h1>${this.addon.name}<h1>
            <p>${this.addon.description}</p>
        </div>
    `;
  }

  async firstUpdated() {
    const a = await getAddon('url_identifier');
    this.addon = a;
  }
}
