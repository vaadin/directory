import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

@customElement('addon-icon')
export class AddonIcon extends LitElement {

  @property()
  src?: string;

  static styles = css`
    :host {
      width: 4rem;
      height: 4rem;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0;
      background-color: var(--icon-background-color, var(--blue-500));
      border-radius: var(--roundness-lg);
    }

    img {
      width: 50%;
      object-fit: contain;
      --filter: grayscale() contrast(150%);
      filter: var(--filter);
      mix-blend-mode: hard-light;
      border-radius: var(--roundness-md);
    }
  `;

  connectedCallback() {
    super.connectedCallback();
    this.setAttribute('role', 'figure');
  }

  render() {
    return html`
      <img src="${this.src}" alt="" />
    `;
  }

}

const convertImageUrlToNumber = (url:string): number => {
  return parseInt(url.replace(/[^0-9]/g, '').substr(0, 20)) || 0;
}
