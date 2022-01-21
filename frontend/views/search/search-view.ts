import '@vaadin/grid';
import './addon-card';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import { searchStore } from './search-store';
import { appStore } from 'Frontend/stores/app-store';
import { disqusReset } from '../disqus';

@customElement('search-view')
export class SearchView extends View {

  constructor() {
    super();
    appStore.currentViewTitle = 'Search';
  }

  render() {
    return html`
      <article class="featured-addons" aria-label="featured add-ons">
        <h2>
          <span>Our favorites</span><span class="sr-only">.</span>
          Featured add-ons
        </h2>
        <section class="gallery">
          ${searchStore.featured.map(
            (addon) => html`<addon-card .addon=${addon}></addon-card>`
          )}
        </section>
      </article>

      <form role="search">
        <select
          .value="${searchStore.sort}"
          @change="${this.updateSort}">
            <option value="recent">New and noteworthy</option>
            <option value="rating">Popular</option>
        </select>
        <input
          type="search"
          placeholder="Search"
          .value=${searchStore.query}
          @input=${this.debounce((e: any) => this.updateQuery(e))} />
        <p>
          <b>${searchStore.totalCount}</b> add-ons found.
          <i class="text-2xs">Want to narrow down? Try filters like <a href="?q=v%3A8">v:8</a> or
          <a href="?q=author%3Ame">author:me</a></i>
        </div>
      </form>

      <section class="results" @filter-added=${this.filterAdded}>
        ${searchStore.addons.map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </section>

      <button
        id="load-more-button"
        @click=${searchStore.fetchPage}
        ?disabled=${searchStore.loading}
        ?hidden=${searchStore.addons.length === 0 || !searchStore.hasMore }>
        Load more
      </button>
    `;
  }

  async firstUpdated() {
    searchStore.init();
    this.setupIntersectionObserver();

    // Reset discuss thread
    disqusReset(
      'search',
      'https://directory4.demo.vaadin.com',
      'Vaadin Directory Search',
      false
    );
    this.restoreScrollIfNeeded();
  }

  restoreScrollIfNeeded() {
    if (window.searchScroll && window.searchScroll > 0) {
      // TODO: Really, this trick again...
      setTimeout(function () {
        window.scroll(0, window.searchScroll);
      }, 0);
    }
  }


  setupIntersectionObserver() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) searchStore.fetchPage();
      });
    });
    const button = this.renderRoot.querySelector('#load-more-button');
    if (button) {
      observer.observe(button);
    }
  }

  filterAdded({ filter }: FilterAddedEvent) {
    searchStore.addFilter(filter);
  }

  updateQuery(e: { target: HTMLInputElement }) {
    searchStore.setQuery(e.target.value);
  }

  updateSort(e: { target: HTMLInputElement }) {
    searchStore.setSort(e.target.value);
  }

  debounce(func : Function, timeout = 500){
    let timer: any;
    return (...args: any[]) => {
      clearTimeout(timer);
      timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
  }
}
