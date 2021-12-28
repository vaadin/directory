import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import '@vaadin/vaadin-lumo-styles/badge';
import '@vaadin/icon';
import '@vaadin/icons';

const OFFICIAL_TAG = "Sponsored";

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
            ${this.addon.tags.map(
              (tag) =>
                html`
                  <vaadin-button style="cursor:pointer;"
                    @click=${() => this.addTagFilter(tag)}
                    theme="badge pill">
                    ${tag}
                    ${OFFICIAL_TAG === tag ? html`<span class="m-xs"><vaadin-icon icon="vaadin:vaadin-v"></vaadin-icon></span>`:''}
                  </vaadin-button>
                `
            )}
          </div>
        </div>

        <h3 class="mb-s">
          <a href="/addon/${this.addon.urlIdentifier}" class="text-body">
            ${this.addon.name}
          </a>
        </h3>
        <div class="text-s text-secondary">by 
          <vaadin-button theme="tertiary" @click=${this.addAuthorFilter}>
          ${this.addon.author}
        </vaadin-button> </div>

        <div>${this.addon.summary}</div>

        <div>${this.addon.rating > 0 ?
                (this.addon.rating < 5 ?
                  '★️'.repeat(this.addon.rating) + '☆'.repeat(5-this.addon.rating):
                  '★️'.repeat(this.addon.rating) ) :
                '☆☆☆☆☆' }</div>
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
}
