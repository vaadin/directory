import './search-view.css';
import '../../components/addon-card';
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

  connectedCallback() {
    super.connectedCallback();
    this.addEventListener('click', this._clickListener);
  }

  disconnectedCallback() {
    this.removeEventListener('click', this._clickListener);
    super.disconnectedCallback();
  }

  render() {
    return html`
      <form role="search" id="search">
        <select
          .value="${searchStore.sort}"
          @change="${this.updateSort}">
            <option value="recent">New and noteworthy</option>
            <option value="rating">Popular</option>
        </select>
        <input
          type="search"
          placeholder="Search"
          .value="${searchStore.query}"
          @input="${this.debounce((e: any) => this.updateQuery(e))}" />
        <select
         .value="${searchStore.version}"
         @change="${this.updateVersion}">
           <option value="all">For all Vaadin versions</option>
           <option value="22">Vaadin 22</option>
           <option value="14">Vaadin 14</option>
           <option value="8">Vaadin 8</option>
           <option value="7">Vaadin 7</option>
           <option value="6">Vaadin 6</option>
        </select>
        <p>
          <b>${searchStore.totalCount >=0 ? searchStore.totalCount : '0'}</b> add-ons found.
          ${searchStore.totalCount > 50 ? html`<i class="text-2xs">Want to narrow down? Try filters like <a href="?q=v%3A8">v:8</a> or
          <a href="?q=author%3Ame">author:me</a></i>` : html``}
        </p>
      </form>

      <section class="results" @filter-added="${this.filterAdded}">
        ${searchStore.addons.map(
          (addon) => addon ? html`
            <addon-card .addon=${addon} .featured=${searchStore.featured.includes(addon.urlIdentifier)}></addon-card>`
            : html`<i>no addons found</i>`
        )}
      </section>

      <button
        id="load-more-button"
        @click="${searchStore.fetchPage}"
        ?disabled="${searchStore.loading}"
        ?hidden="${searchStore.addons.length === 0 || !searchStore.hasMore }">
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
    // TODO: workaround for an issue in Vaadin Router, which scrolls the page to the top before updating the browser location
    setTimeout(function () {
      window.scroll(0, appStore.searchViewScrollTop);
    }, 0);
  }

  _clickListener() {
    appStore.searchViewScrollTop = window.scrollY;
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

  updateVersion(e: { target: HTMLInputElement }) {
    searchStore.setVersion(e.target.value);
  }

  debounce(func : Function, timeout = 500){
    let timer: any;
    return (...args: any[]) => {
      clearTimeout(timer);
      timer = setTimeout(() => { func.apply(this, args); }, timeout);
    };
  }
}
