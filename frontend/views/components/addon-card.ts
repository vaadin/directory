import {
  BeforeEnterObserver,
  PreventAndRedirectCommands,
  Router,
  RouterLocation,
} from '@vaadin/router';
import { AddonEndpoint } from 'Frontend/generated/endpoints';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('addon-card')
export class AddonCard extends View implements BeforeEnterObserver {
  @property({ attribute: false })
  addon?: Addon;

  constructor() {
    super();
    this.classList.add(
      'flex',
      'flex-col',
      'gap-s',
      'p-l',
      'border',
      'border',
      'border-contrast-20',
      'rounded-m'
    );
  }

  async onBeforeEnter(location: RouterLocation) {
    this.addon = await AddonEndpoint.getAddon(
      location.params['addon'] as string
    );
  }

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    return html`
      <a href="/addon/${this.addon.urlIdentifier}" class="text-body">
        <div class="flex justify-between">
          <img style="width: 64px; height: 64px" src="https://vaadin.com/static/portrait/initials/a" alt=${this.addon.name} />
          <div class="flex flex-row-reverse gap-xs flex-wrap">
            ${this.addon.tags.map(
              (tag) =>
                html`
                  <div class="text-s border border-contrast-40 p-xs rounded-s">
                    ${tag}
                  </div>
                `
            )}
          </div>
        </div>

        <h3 class="mb-s">${this.addon.name}</h3>
        <div class="text-s text-secondary">by ${this.addon.author}</div>

        <div>${this.addon.summary}</div>

        <div>${'★️'.repeat(this.addon.rating)}</div>
      </a>
    `;
  }
}
