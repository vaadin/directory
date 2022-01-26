import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('highlight-carousel')
export class HighlightCarousel extends View {

  @property({ attribute: false })
  addon?: Addon;

  firstUpdated() {
    this.classList.toggle('empty', this.addon?.mediaHighlights.length === 0);
  }

  render() {
    return html`
      ${this.addon?.mediaHighlights.map((m,i) => html`
        <div class="item" id="highlight-${i}">
          <img src="${m?.url}">
          <a class="open" href="/addon/${this.addon?.urlIdentifier}#highlight-${i}">View larger</a>
          <a class="close" href="/addon/${this.addon?.urlIdentifier}#_">Close</a>
        </div>
      `)}
    `;
  }
}
