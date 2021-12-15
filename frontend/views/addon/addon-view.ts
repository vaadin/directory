import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { View } from '../view';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import {
  BeforeEnterObserver,
  PreventAndRedirectCommands,
  Router,
  RouterLocation,
} from '@vaadin/router';

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {
  @state()
  private addon?: Addon;

  render() {
    if (!this.addon) {
      return html`Loading...`;
    }

    return html`
      <div>
        <h1>${this.addon.name}</h1>
        <div>${this.addon.author}</div>
        <p>${this.addon.summary}</p>
      </div>
    `;
  }

  async onBeforeEnter(location: RouterLocation) {
    const urlIdentifier = location.params.addon as string;
    this.addon = await getAddon(urlIdentifier);
  }
}
