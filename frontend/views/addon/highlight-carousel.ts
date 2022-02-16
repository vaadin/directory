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
        <div class="item item-${i}" @keydown="${this.closeOnEsc}" tabindex="0">
          <img src="${m?.url}" @click="${this.close}">
          <button class="btn-open" @click="${this.open}">View larger</button>
          <button class="btn-close" @click="${this.close}">Close</button>
        </div>
      `)}
    `;
  }

  open(e:MouseEvent) {
    const el = e.target as HTMLButtonElement;
    const item = el.parentElement;
    if (item) {

      // Pop up from mouse x/y
      item.style.setProperty('--x', ''+e.clientX+'px');
      item.style.setProperty('--y', ''+e.clientY+'px');
      item.classList.add('item-open-start');

      // Allow previous class to render before applying new
      setTimeout(function () {
        item.classList.add('item-open');
        item.focus();
      }, 0);

      // remove start position styles
      setTimeout(function () {
        item.classList.remove('item-open-start');
      }, 200);
    }
  }

  closeOnEsc(e: KeyboardEvent) {
    if (e.keyCode == 27) { this.close(); }
  }

  close() {
    this.querySelectorAll('.item').forEach(item => item.classList.remove('item-open'));
  }

}
