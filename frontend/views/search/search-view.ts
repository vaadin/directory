import './search-view.css';
import '../../components/addon-card';
import { html, nothing } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View, PageJsonLd } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import { searchStore } from './search-store';
import { appStore } from 'Frontend/stores/app-store';
import { RouterLocation } from '@vaadin/router';

@customElement('search-view')
export class SearchView extends View {

  requestedPage: number = -1;

  constructor() {
    super();
    appStore.setCurrentViewTitle('Vaadin Directory Search');
  }

  async onBeforeEnter(location: RouterLocation) {
    const params = new URLSearchParams(location.search);
    this.requestedPage = +(params.get('page') || '-1');
  }

  connectedCallback() {
    super.connectedCallback();
    this.addEventListener('click', this._clickListener);
  }

  disconnectedCallback() {
    this.removeEventListener('click', this._clickListener);
    super.disconnectedCallback();
  }

  updatePageMetadata(): void {
    // Update search metadata
    const metadata = new PageJsonLd(appStore.applicationName, appStore.appDescription, appStore.appUrl);
    metadata.appendOrReplaceToHead();

    // Update Canonical URL
    const canonical = document.head.querySelector('link[rel="canonical"]') as HTMLElement; 
    if (canonical) canonical.setAttribute("href",appStore.appUrl);

    // Update Twitter metadata
    const title = document.head.querySelector('meta[name="twitter:title"]') as HTMLElement; 
    const summary = document.head.querySelector('meta[name="twitter:description"]') as HTMLElement; 
    const icon = document.head.querySelector('meta[name="twitter:image"]') as HTMLElement; 
    if (title) title.setAttribute("content","Vaadin Add-on Directory");
    if (summary) summary.setAttribute("content","Find open-source widgets and components for your Vaadin application.");
    if (icon) icon.setAttribute("content","https://vaadin.com/images/trademark/PNG/VaadinLogomark_RGB_500x500.png");

  }

  render() {

    // Update prev and next links
    if (this.requestedPage > 1) {
      let linkPrev = document.head.querySelector('link[id="link-prev"]') as HTMLLinkElement || document.createElement("link");
      linkPrev.id = "link-prev";
      linkPrev.rel = "prev";
      linkPrev.href= searchStore.getPrevPageURL();
      document.head.appendChild(linkPrev);
    } else {
      let linkPrev = document.head.querySelector('link[id="link-prev"]') as HTMLLinkElement;
      if (linkPrev) document.head.removeChild(linkPrev);
    }
    if (this.requestedPage < searchStore.totalPages && this.requestedPage > 0) {
      let linkNext = document.head.querySelector('link[id="link-next"]') as HTMLLinkElement || document.createElement("link");
      linkNext.id = "link-next";
      linkNext.rel = "next";
      linkNext.href= searchStore.getNextPageURL();
      document.head.appendChild(linkNext);
    } else if (this.requestedPage < 1) {
      // no paging, show anyway with static page
      let linkNext = document.head.querySelector('link[id="link-next"]') as HTMLLinkElement || document.createElement("link");
      linkNext.id = "link-next";
      linkNext.rel = "next";
      linkNext.href= searchStore.getPageURL(1);
      document.head.appendChild(linkNext);
    } else {
      // remove from last page
      let linkNext = document.head.querySelector('link[id="link-next"]') as HTMLLinkElement;
      if (linkNext) document.head.removeChild(linkNext);
    }

    return html`
      <form
        role="search"
        id="search"
        onsubmit="event.preventDefault(); document.activeElement.blur();">
        <div class="search-input">
          <input
            autofocus
            type="search"
            placeholder="Search add-ons"
            aria-label="Search add-ons"
            enterkeyhint="Search"
            autocomplete="off"
            autocapitalize="off"
            autocorrect="off"
            spellcheck="false"
            .value="${searchStore.query}"
            @input="${this.debouncedUpdateQuery}" />
          <select
            class="vaadin-version-select"
            .value="${searchStore.version}"
            @change="${this.updateVersion}"
            aria-label="Vaadin version">
            <option value="all">for all versions</option>
            <option value="24">Vaadin 24</option>
            <option value="23">Vaadin 23</option>
            <option value="22">Vaadin 22</option>
            <option value="14">Vaadin 14</option>
            <option value="8">Vaadin 8</option>
            <option value="7">Vaadin 7</option>
            <option value="6">Vaadin 6</option>
          </select>
        </div>
        <select
          class="sort-select"
          .value="${searchStore.sort}"
          @change="${this.updateSort}"
          aria-label="Sorting">
          <option value="recent">New &amp; noteworthy</option>
          <option value="rating">Popular</option>
          <option value="alphabetical">A to Z</option>
        </select>
        <p>
          <b>${searchStore.totalCount >= 0 ? searchStore.totalCount : '0'}</b>
          add-ons found.
          ${this.requestedPage > 0 && searchStore.totalPages > 0? 
            html`(Showing page ${searchStore.page}/${searchStore.totalPages})`
            : nothing }
        </p>
      </form>

      <section class="results" @filter-added="${this.filterAdded}">
        ${searchStore.addons.map((addon) =>
          addon
            ? html` <addon-card
                .addon=${addon}
                .featured=${searchStore.featured.includes(
                  addon.urlIdentifier
                )}></addon-card>`
            : html`<i>no addons found</i>`
        )}
      </section>

      
      <section id="paging">
      ${this.requestedPage > 0 && searchStore.totalPages > 0? 
      html`
        <a router-ignore href="${searchStore.getPrevPageURL()}"
          id="prev-button"
          ?disabled="${searchStore.loading || searchStore.isFirst}"
          ?hidden="${searchStore.addons.length === 0 && this.requestedPage < 1}">
          Previous page
        </a>`: nothing }

      <section id="current-page" ?hidden="${this.requestedPage < 1 && searchStore.totalPages < 0}">
        ${this.requestedPage > 0 && searchStore.totalPages > 0? 
          html` ${searchStore.page}/${searchStore.totalPages} ` : nothing }
      </section>

      <section id="page-links" ?hidden="${this.requestedPage < 1 || searchStore.totalPages < 0}">
        ${searchStore.totalPages > 0 ? 
          html`Jump to page: 
            ${Array.from({length: Math.min(searchStore.totalPages, 10)}, (x,i) => html` <a router-ignore href="${searchStore.getPageURL(i+1)}">${i+1}</a> `)}
            ${searchStore.totalPages > 5? html` ... <a router-ignore href="${searchStore.getPageURL(searchStore.totalPages)}">${searchStore.totalPages}</a>`: nothing }
            `: nothing }
      </section>

      <a router-ignore href="${searchStore.getNextPageURL()}"
          id="next-button"
          ?disabled="${searchStore.loading || !searchStore.hasMore || (searchStore.totalPages > 0 && searchStore.page === searchStore.totalPages)}"
          ?hidden="${!searchStore.hasMore }">Next page</a>
      </section>
    `;
    
  }

  async firstUpdated() {
    searchStore.init();
    this.setupIntersectionObserver();
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
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && this.requestedPage < 1) {
            searchStore.fetchNextPage(false);
          }
        });
      },
      { rootMargin: '300px' }
    );
    const button = this.renderRoot.querySelector('#next-button');
    if (button) {
      observer.observe(button);
    }
  }

  filterAdded({ filter }: FilterAddedEvent) {
    searchStore.addFilter(filter);
  }

  updateQuery(query: string) {
    searchStore.setCurrentUser(this.getCurrentUserId());
    if (query && query.length > 2) searchStore.setQuery(query);
  }

  updateSort(e: { target: HTMLInputElement }) {
    searchStore.setSort(e.target.value);
  }

  updateVersion(e: { target: HTMLInputElement }) {
    searchStore.setVersion(e.target.value);
  }

  private debouncedUpdateQuery = this.debounce((e: Event) => {
      const value = (e.target as HTMLInputElement).value;
      this.updateQuery(value);
  });

  private debounce<F extends (...args: any[]) => void>(func: F, delay = 300) {
    let timer: number;
    return (...args: Parameters<F>) => {
      clearTimeout(timer);
      timer = window.setTimeout(() => func(...args), delay);
    };
  }

}
