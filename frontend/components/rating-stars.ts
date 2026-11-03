import { LitElement, html, svg, css, nothing } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';

const MAX_STARS = 5;
const STAR_WIDTH_EM = 1.1;

// One shared path for empty (outlined) and filled stars, so both layers match exactly.
const STAR = html`<svg viewBox="0 0 24 24" aria-hidden="true">${svg`<path d="M12 2.5l2.94 6.06 6.56.95-4.75 4.63 1.12 6.53L12 17.6l-5.87 3.07 1.12-6.53L2.5 9.51l6.56-.95z"/>`}</svg>`;

@customElement('rating-stars')
export class RatingStars extends LitElement {

  /** Display only (cards, grids, stats). */
  @property({ type: Boolean })
  readonly: boolean = false;

  /** Rating is possible, but the user must log in first. Shows a "Log in to rate" link. */
  @property({ type: Boolean })
  loginrequired: boolean = false;

  @property({ type: Number })
  userrating: number = 0;

  @property()
  tooltip: string = "";

  @property({ type: Number })
  avgrating: number = 0;

  @property({ type: Number })
  ratingcount: number = 0;

  @property({ type: Number })
  minNumberOfRatings: number = 3;

  /** Star currently hovered or focused, 0 when none. */
  @state()
  private hoverValue: number = 0;

  static styles = css`
    :host {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0 0.4em;
      font-weight: var(--text-weight-semibold);
    }

    .stars {
      display: inline-grid;
      vertical-align: middle;
    }

    /* Empty and filled stars are stacked in the same grid cell and use identical boxes and
       the same SVG path, so the filled layer covers the empty one exactly. */
    .layer {
      grid-area: 1 / 1;
      display: flex;
    }

    .fill {
      justify-self: start;
      overflow: hidden;
      pointer-events: none;
    }

    .star {
      flex: 0 0 auto;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      box-sizing: border-box;
      width: ${STAR_WIDTH_EM}em;
      height: ${STAR_WIDTH_EM}em;
      margin: 0;
      padding: 0;
      border: none;
      background: none;
      color: inherit;
      font: inherit;
    }

    .star svg {
      display: block;
      width: 1em;
      height: 1em;
      stroke: currentColor;
      stroke-width: 1.5;
      stroke-linejoin: round;
    }

    .empty svg {
      fill: none;
    }

    .fill svg {
      fill: currentColor;
    }

    button.star {
      cursor: pointer;
      transition: transform 0.2s ease;
    }

    button.star:active {
      transform: scale(1.2);
    }

    button.star:focus-visible {
      outline: 2px solid var(--blue-500);
      outline-offset: 1px;
      border-radius: 2px;
    }

    .user-rating .fill,
    .preview .fill {
      color: var(--blue-500);
    }

    .text,
    .no-rating {
      white-space: nowrap;
      font-size: var(--text-size-s);
    }

    .text {
      font-weight: var(--text-weight-regular);
    }

    .no-rating {
      font-weight: var(--text-weight-semibold);
    }

    .login {
      background: none;
      border: none;
      padding: 0;
      font: inherit;
      font-size: var(--text-size-s);
      font-weight: var(--text-weight-regular);
      color: var(--blue-500);
      text-decoration: underline;
      cursor: pointer;
      white-space: nowrap;
    }
  `;

  render() {
    const interactive = this.isInteractive();
    const enough = this.hasEnoughRatings();
    const rated = this.userrating > 0;
    const hovering = interactive && this.hoverValue > 0;
    // Without enough ratings there is no average worth showing: read-only falls back to a
    // text label and the login state shows only the login link.
    const showStars = enough || interactive;

    let filled: number;
    let mode: string;
    if (hovering) {
      filled = this.hoverValue;
      mode = 'preview';
    } else if (interactive && rated) {
      filled = this.userrating;
      mode = 'user-rating';
    } else {
      filled = enough ? this.avgrating : 0;
      mode = 'average';
    }

    // The average itself is only shown as a tooltip on the stars.
    let text: unknown = nothing;
    if (hovering) {
      text = `Rate ${this.hoverValue}/5`;
    } else if (this.readonly) {
      text = enough ? `(${this.ratingcount})` : nothing;
    } else if (interactive && !rated) {
      text = 'Click to rate';
    }

    return html`
      ${showStars ? this.renderStars(filled, mode, interactive)
        : this.readonly ? html`<span class="no-rating" title="${this.tooltip}">Not enough ratings</span>` : nothing}
      ${text !== nothing ? html`<span class="text">${text}</span>` : nothing}
      ${this.loginrequired && !this.readonly ? html`<button class="login" @click="${this._handleLogin}">Log in to rate</button>` : nothing}
    `;
  }

  private renderStars(filled: number, mode: string, interactive: boolean) {
    const values = Array.from({ length: MAX_STARS }, (_, i) => i + 1);
    const title = this.tooltip || (this.hasEnoughRatings() ? `Average ${this.avgrating.toFixed(1)}` : '');
    const fill = html`
      <span class="layer fill" style="width: ${Math.max(0, Math.min(MAX_STARS, filled)) / MAX_STARS * 100}%" aria-hidden="true">
        ${values.map(() => html`<span class="star">${STAR}</span>`)}
      </span>`;

    if (!interactive) {
      const label = this.hasEnoughRatings()
        ? `Average ${this.avgrating.toFixed(1)} of 5 from ${this.ratingcount} ratings`
        : 'Not enough ratings';
      return html`
        <span class="stars ${mode}" role="img" aria-label="${label}" title="${title}">
          <span class="layer empty" aria-hidden="true">
            ${values.map(() => html`<span class="star">${STAR}</span>`)}
          </span>
          ${fill}
        </span>`;
    }

    return html`
      <span class="stars ${mode}" title="${title}">
        <span class="layer empty" role="radiogroup" aria-label="Your rating"
              @mouseleave="${this._clearHover}" @keydown="${this._handleKeyDown}">
          ${values.map(v => html`
            <button class="star" role="radio" aria-checked="${v === this.userrating}"
                    aria-label="Rate ${v} of 5" data-value="${v}"
                    @mouseenter="${() => this.hoverValue = v}"
                    @focus="${() => this.hoverValue = v}"
                    @blur="${this._clearHover}"
                    @click="${() => this._rate(v)}">${STAR}</button>`)}
        </span>
        ${fill}
      </span>`;
  }

  hasEnoughRatings(): boolean {
    return this.ratingcount >= this.minNumberOfRatings;
  }

  private isInteractive(): boolean {
    return !this.readonly && !this.loginrequired;
  }

  private _clearHover() {
    this.hoverValue = 0;
  }

  private _handleKeyDown(e: KeyboardEvent) {
    const delta = e.key === 'ArrowRight' || e.key === 'ArrowUp' ? 1
      : e.key === 'ArrowLeft' || e.key === 'ArrowDown' ? -1 : 0;
    if (!delta) return;
    e.preventDefault();
    const current = this.hoverValue || this.userrating || 0;
    const next = Math.max(1, Math.min(MAX_STARS, current + delta));
    this.renderRoot.querySelector<HTMLButtonElement>(`button.star[data-value="${next}"]`)?.focus();
  }

  private _rate(newRating: number) {
    if (!this.isInteractive()) return;
    if (this.userrating != newRating) {
      this.userrating = newRating;
      this.fireRatingEvent();
    }
  }

  private _handleLogin() {
    this.dispatchEvent(new Event('login-request', { bubbles: true, composed: true }));
  }

  fireRatingEvent() {
    this.dispatchEvent(new RatingEvent(this.userrating));
  }
}

/* Custom rating event. */
export class RatingEvent extends Event {

    rating: number;

    constructor(rating: number) {
      super("rating");
      this.rating = rating;
    }
}
