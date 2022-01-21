import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';

const OFFICIAL_TAG = "Sponsored";
const FEATURED_TAG = "Featured";

@customElement('addon-card')
export class AddonCard extends View {


  @property({ attribute: false })
  addon?: SearchResult;

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    return html`
          <figure>
            <img src="${this.addon.icon}" alt="" />
          </figure>

          <section class="content">
            <h3>
              <a href="/addon/${this.addon.urlIdentifier}" @click="${this.onClick}" class="text-body">
                ${this.addon.name}
              </a>
            </h3>

            <button class="author" @click=${this.addAuthorFilter}>
              ${this.addon.author}
            </button>

            <section class="rating">
              ${this.addon.rating > 0 ?
                (this.addon.rating < 5 ?
                '★️'.repeat(this.addon.rating) + '☆'.repeat(5-this.addon.rating):
                '★️'.repeat(this.addon.rating) ) :
                'No ratings yet' }
              <span>${this.addon.ratingCount}</span>
            </section>

            <p class="summary">${this.addon.summary}</p>
          </section>
    `;
  }

  addTagFilter(tag: string) {
    this.dispatchEvent(
      new FilterAddedEvent({
        type: 'tag',
        value: tag,
      })
    );
  }

  onClick() {
    window.searchScroll = window.scrollY;
  }

  addAuthorFilter() {
    if (this.addon?.author) {
      this.dispatchEvent(
        new FilterAddedEvent({
          type: 'author',
          value: this.addon.author,
        })
      );
    }
  }

  formatDate(date: Date) {
    const now = new Date().setHours(0, 0, 0, 0);
    const then = date.setHours(0, 0, 0, 0);
    const days = (then - now) / 86400000;
    if (days > -14) {
      if (days > -7) {
        return relative.format(days, 'day');
      }
      return relative.format(days, 'week');
    }
    return absolute.format(date);
  }
}

// Formatter for "Today" and "Yesterday" etc
const relative = new Intl.RelativeTimeFormat(
  'en-US', {numeric: 'auto'}
);

// Formatter for dates, e.g. "Mon, 31 May 2021"
const absolute = new Intl.DateTimeFormat(
  'en-US', {
  day: 'numeric',
  month: 'short',
  year: 'numeric'
});


const convertImageUrlToNumber = (url:string): number => {
  return parseInt(url.replace(/[^0-9]/g, '').substr(0, 20)) || 0;
}
