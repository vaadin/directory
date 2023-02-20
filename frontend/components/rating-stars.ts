import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

@customElement('rating-stars')
export class RatingStars extends LitElement {

  @property({ attribute: true })
  readonly: boolean = false;

  @property({ attribute: true })
  userrating: boolean = false;

  @property({ attribute: true })
  tooltip: string = "";

  @property({ attribute: true })
  rating: number = 0;

  @property({ attribute: true })
  ratingcount: number = 0;

  static styles = css`
    :host {
      display: flex;
      flex-wrap: wrap;
      max-width: 110px;
    }

    .user-rating {
      color: var(--blue-500);
    }

    .ratings {
      display: flex;
      width: 110px;
      align-items: center;
      flex-direction: row-reverse;
      position: relative;
      gap: 0 0.1;
      font-weight: var(--text-weight-semibold);
    }

    .no-rating {
      white-space: nowrap;
      font-size: var(--text-size-xs);
    }

    .count,
    .user-score {
      font-weight: var(--text-weight-regular);
      margin-left: .5em;
      width: 100%;
    }

    .select .user-score {

    }

    .user-rating .user-score {
      display: block;
    }

    .ratings input.rating {
       display: none;
    }

    .ratings label {
      vertical-align: bottom;
      width: 1.1em;
      transition: 0.2s ease;
    }

    .ratings.select label {
      color: var(--blue-100);
    }

    .ratings.select label:hover {
      color: var(--blue-500);
    }

    .ratings label:active::before {
      transform:scale(1.2);
    }

    .ratings label::before {
      content: '☆';
    }

    .ratings input:checked ~ label:before {
      content:'★';
    }

    .ratings.select input:checked ~ label::before {
      color: var(--blue-200);
      content: '☆';
    }

    .ratings.select input:hover ~ label:before {
      color: var(--blue-500);
      content:'★';
    }

    .hidden {
       display: none;
    }
  `;

  constructor() {
    super();
  }

  async firstUpdated() {
    // Give the browser a chance to paint
    await new Promise((r) => setTimeout(r, 0));
    this.addEventListener('mouseover', this._handleMouseOver);
    this.addEventListener('mouseleave', this._handleMouseLeave);
  }

  render() {
    return html`
          <div title="${this.tooltip}"  class="ratings ${this.userrating && this.rating > 0?'user-rating':''} ${this.hasEnoughRatings() || this.userrating && this.rating > 0?'':'hidden'}">
            ${this.hasEnoughRatings() && !this.userrating ?
                html`<span class="count">${this.ratingcount}</span>` :
                html`<span class="user-score">${this.userrating  && this.rating > 0 ? this.rating : '-'}/5</span>`}
            <input ?checked=${Math.round(this.rating) === 5} @click="${this._handleClick}" class="rating" type="radio" ?disabled="${this.readonly}" name="stars" id="star-5" value="5"/><label for="star-5"></label>
            <input ?checked=${Math.round(this.rating) === 4} @click="${this._handleClick}" class="rating" type="radio" ?disabled="${this.readonly}" name="stars" id="star-4" value="4"/><label for="star-4"></label>
            <input ?checked=${Math.round(this.rating) === 3} @click="${this._handleClick}" class="rating" type="radio" ?disabled="${this.readonly}" name="stars" id="star-3" value="3"/><label for="star-3"></label>
            <input ?checked=${Math.round(this.rating) === 2} @click="${this._handleClick}" class="rating" type="radio" ?disabled="${this.readonly}" name="stars" id="star-2" value="2"/><label for="star-2"></label>
            <input ?checked=${Math.round(this.rating) === 1} @click="${this._handleClick}" class="rating" type="radio" ?disabled="${this.readonly}" name="stars" id="star-1" value="1"/><label for="star-1"></label>
          </div>
          <div title="${this.tooltip}" class="no-rating ${this.hasEnoughRatings() || this.userrating && this.rating > 0?'hidden':''}">Not enough ratings</div>
      `;
  }


  hasEnoughRatings(): boolean {
    return this.ratingcount > 4;
  }

  _handleClick(e : PointerEvent) {
      if (this.readonly || !this.userrating) return;
      const currentRating = this.rating;
      const newRating = e.target ? parseInt((e.target as HTMLInputElement).value) : currentRating;
      if (currentRating != newRating) {
        this.rating = newRating;
        this.fireRatingEvent();
      }
  }

  _handleMouseOver(e : MouseEvent) {
    if (this.readonly || !this.userrating) return;
    const starsElem = this.renderRoot.querySelector<HTMLElement>(".ratings");
    const nrElem = this.renderRoot.querySelector<HTMLElement>(".no-rating");
    if (starsElem && nrElem) {
        starsElem.classList.add("select");
        nrElem.classList.add("hidden");
        starsElem.classList.remove("hidden");
    }
  }

  _handleMouseLeave(e : MouseEvent) {
    if (this.readonly || !this.userrating) return;
      const starsElem = this.renderRoot.querySelector<HTMLElement>(".ratings");
      const nrElem = this.renderRoot.querySelector<HTMLElement>(".no-rating");
      if (starsElem && nrElem) {
          starsElem.classList.toggle("hidden", !this.hasEnoughRatings() && !this.userrating);
          nrElem.classList.toggle("hidden", this.hasEnoughRatings() || this.userrating);
          starsElem.classList.remove("select");
      }
  }

  fireRatingEvent() {
    this.dispatchEvent(new RatingEvent(this.rating));
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
