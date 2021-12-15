import {
  BeforeEnterObserver,
  PreventAndRedirectCommands,
  Router,
  RouterLocation,
} from '@vaadin/router';
import { AddonEndpoint } from 'Frontend/generated/endpoints';
import Addon from 'Frontend/generated/org/vaadin/directory/search/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';

@customElement('addon-card')
export class AddonCard extends View {
  @property({ attribute: false })
  addon?: Addon;

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
          <img style="width: 64px; height: 64px" src="https://vaadin.com/static/portrait/initials/a" alt=${
            this.addon.name
          } />
          <div class="flex flex-row-reverse gap-xs flex-wrap">
            ${this.addon.tags.map(
              (tag) =>
                html`
                  <vaadin-button
                    @click=${() => this.addTagFilter(tag)}
                    theme="small">
                    ${tag}
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

        <div>${'★️'.repeat(this.addon.rating)}</div>
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
