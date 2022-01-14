import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import '@vaadin/vaadin-lumo-styles/badge';
import '@vaadin/icon';
import '@vaadin/icons';

const OFFICIAL_TAG = "Sponsored";
const FEATURED_TAG = "Featured";

@customElement('addon-card')
export class AddonCard extends View {


  @property({ attribute: false })
  addon?: SearchResult;

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
          <img style="width: 64px; height: 64px" src="${this.addon.icon}" alt=${
            this.addon.name
          } />
          <div class="flex flex-row-reverse gap-xs flex-wrap">
            ${this.addon.tags.filter(e => FEATURED_TAG !== e).map(
              (tag) =>
                html`
                  <vaadin-button style="cursor:pointer;"
                    @click=${() => this.addTagFilter(tag)}
                    theme="badge pill">
                    ${OFFICIAL_TAG === tag ? html`${tag} <i class="fa-solid fab fa-vaadin"></i>`:''}
                    ${OFFICIAL_TAG !== tag ? html`${tag}`:''}
                  </vaadin-button>
                `
            )}
          </div>
        </div>

        <h3 class="mb-s">
          <a href="/addon/${this.addon.urlIdentifier}" @click="${this.onClick}" class="text-body">
            ${this.addon.name}
          </a>
        </h3>
        <div class="text-s text-secondary">by 
          <vaadin-button theme="tertiary" @click=${this.addAuthorFilter}>
          ${this.addon.author}
        </vaadin-button> </div>

        <div>${this.addon.summary}</div>

        <div><span class="rating">${this.addon.rating > 0 ?
                (this.addon.rating < 5 ?
                  '★️'.repeat(this.addon.rating) + '☆'.repeat(5-this.addon.rating):
                  '★️'.repeat(this.addon.rating) ) :
                '☆☆☆☆☆' }</span> <span class="updated" title="${this.addon.lastUpdated}">${this.formatDate(new Date(this.addon.lastUpdated))}</span></div>
      </a>

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



