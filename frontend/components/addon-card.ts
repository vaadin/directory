import './addon-card.css';
import { Layout } from '../views/view';
import { FilterAddedEvent } from '../views/search/filter-added-event';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import './addon-icon';
import './rating-stars';

@customElement('addon-card')
export class AddonCard extends Layout {

  @property({ attribute: false })
  addon?: SearchResult;

  @property({ attribute: false })
  featured: boolean = false;

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    this.classList.toggle('featured', this.featured);
    return html`
      ${this.featured ? html`<b class="badge featured">Featured</b>` : ''}
      <addon-icon src="${this.addon.icon}"></addon-icon>

      <section class="content">
        <h3>
          <a href="component/${this.addon.urlIdentifier}" class="text-body">
            ${this.addon.name}
          </a>
        </h3>

        <div class="author">${this.addon.author}</div>

        <rating-stars avgrating="${this.addon.rating}" ratingcount="${this.addon.ratingCount}" readonly="true"></rating-stars>

        <p class="summary">${this.addon.summary}</p>
      </section>
    `;
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
