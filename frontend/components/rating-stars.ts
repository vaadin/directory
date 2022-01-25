import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

@customElement('rating-stars')
export class RatingStars extends LitElement {

  @property({ attribute: false })
  rating: number = 0;

  @property({ attribute: false })
  ratingCount: number = 0;

  static styles = css`
    :host {
      display: flex;
      align-items: center;
      gap: 0 0.3em;
      color: var(--secondary-text-color);
    }

    .stars {
      font-size: 1.125em;
    }
  `;

  render() {
    if (this.ratingCount === undefined || this.ratingCount < 3) {
      return html`Not enough ratings`;
    }
    const stars = this.rating < 5 ? '★️'.repeat(Math.round(this.rating)) + '☆'.repeat(5-Math.round(this.rating)) : '★️'.repeat(this.rating);
    return html`<span class="stars">${stars}</span> ${this.ratingCount}`;
  }

}
