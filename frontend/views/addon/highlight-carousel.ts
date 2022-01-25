import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('highlight-carousel')
export class HighlightCarousel extends View {

  @property({ attribute: false })
  addon?: Addon;

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    return html`
      ${this.addon.mediaHighlights.map((m,i) => html`
        <div><img src="${m?.url}"></img></div>
      `)}
    `;
  }
}
