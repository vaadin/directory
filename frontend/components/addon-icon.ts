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
      background-color: var(--blue-500);
      border-radius: var(--roundness-lg);
    }

    img {
      width: 50%;
      object-fit: contain;
      --filter: grayscale() contrast(100%);
      filter: var(--filter);
      mix-blend-mode: screen;
      border-radius: var(--roundness-md);
    }

    :host(.light) img {
      mix-blend-mode: multiply;
    }
  `;

  firstUpdated() {
    this.setAttribute('role', 'figure');
    const num = convertImageUrlToNumber(this.src);
    const color = colors[num % colors.length];
    const shade = shades[num % shades.length];
    this.style.setProperty('background-color', `var(--${color}-${shade})`);
    if (shade <= 300) {
      this.classList.add('light')
    }
  }

  render() {
    return html`
      <img src="${this.src}" alt="" />
    `;
  }

}

const colors = ['blue', 'indigo', 'violet', 'green', 'orange', 'yellow', 'red'];
const shades = [100, 200, 300, 400, 500, 600];

const convertImageUrlToNumber = (url:string|undefined): number => {
  if (url) {
    return parseInt(url.replace(/[^0-9]/g, '').substr(0, 10)) || 0;
  }
  return 0;
}
