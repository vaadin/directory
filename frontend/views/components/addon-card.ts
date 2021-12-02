import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('addon-card')
export class AddonCard extends View {
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

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    return html`
      <div class="flex justify-between">
        <img src="" alt=${this.addon.name} />
        <div class="flex flex-row-reverse gap-xs flex-wrap">
          ${this.addon.tags.map(
            (tag) =>
              html`
                <div class="border border-contrast-40 p-xs rounded-s">
                  ${tag}
                </div>
              `
          )}
        </div>
      </div>

      <h3 class="mb-s">${this.addon.name}</h3>
      <div class="text-s text-secondary">${this.addon.author}</div>

      <div>${this.addon.description}</div>

      <div>${'⭐️'.repeat(this.addon.rating)}</div>
    `;
  }
}
