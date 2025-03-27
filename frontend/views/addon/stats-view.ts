import './addon-view.css';
import '../../components/addon-icon';
import { RatingStars, RatingEvent } from '../../components/rating-stars';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { getAddon, getLatestVersion, getUserRating, getAddonInstallCountExact, getAddonMavenDownloadCount, getVisits, getCountries } from 'Frontend/generated/AddonEndpoint';
import { html, css, nothing, render } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { guard } from 'lit/directives/guard.js';
import '@vaadin/avatar/src/vaadin-avatar';
import { appStore } from 'Frontend/stores/app-store';
import { router } from '../../index';
import { View } from '../view';

@customElement('addon-stats-view')
export class StatsView extends View implements BeforeEnterObserver {

  @state()
  private addon?: Addon;

  @state()
  private version?: AddonVersion;

  @state()
  private addonInstallCount: number = 0;

  @state()
  private addonMavenDownloadCount: number = 0;

  @state()
  private addonVisits: number[] = [];

  @state()
  private addonCountries: Map<string, number> = new Map();

  @state()
  private viewDateRange: number = 30;

  constructor() {
    super();
  }

  updatePageMetadata(): void {

  }

  async onBeforeEnter(location: RouterLocation) {
      const urlIdentifier = location.params.addon as string;
      const urlVersion = location.params.version as string;
      this.addon = await getAddon(urlIdentifier, this.getCurrentUserId());
      if (this.addon) {
        if (urlVersion) {
          const found = this.addon?.versions.find(
            (v: AddonVersion) => v.name == urlVersion
          );
          this.version = found ? found : this.getLatestVersion();
        } else {
          this.version = this.getLatestVersion();
        }
        appStore.setCurrentViewTitle("Stats for "+this.addon.name);
        this.updatePageMetadata();
      }
  }

  render() {
    if (!this.addon || !this.version) {
      return html`<section class="main"><h3>Hmm... That add-on was not found.</h3>
      <div>${appStore.appUrl? html`<a href="${appStore.appUrl}">Try search for it instead.</a>`:''}</div></section>`;
    }

    const max = Math.max(...this.addonVisits, 1); // Avoid division by zero

    return html`
          <section class="main">
            <addon-icon src="${this.addon.icon}"></addon-icon>
            <h2 class="addon-name">${this.addon.name}</h2>
            <p class="addon-summary">${this.addon.summary}</p>

        <section class="meta">
          <section class="author">
            <h3>Author</h3>
            <button @click="${this.searchByAuthor}">
              <vaadin-avatar .img="${this.addon.authorImage}" name="${this.addon.author}"></vaadin-avatar>
              ${this.addon.author}
            </button>
          </section>

          ${this.getGitHubLink() ? html`
            <section class="contributors">
              <github-contributors repositoryUrl="${this.getGitHubLink()}"></github-contributors>
            </section>
          `:nothing}

          <section class="rating">
            <h3>Rating</h3>
            <rating-stars
              id="rating-stars"
              readonly="true"
              minNumberOfRatings="1"
              avgrating="${this.addon.rating.toFixed(1)}"
              ratingcount="${this.addon.ratingCount}">
            </rating-stars>
          </section>

          <section class="popularity" title="Average rating">
              <h3>Average</h3>
              ${this.addon.ratingCount > 0 ? this.addon.rating.toFixed(2) : 'n/a'}
          </section>
        </section>

            <section class="statistics">
                <div class="installs"><h4>Total user installs:</h4> ${this.addonInstallCount}</div>
                <div class="downloads"><h4>Maven Downloads:</h4> ${this.addonMavenDownloadCount > 0 ? this.addonMavenDownloadCount : 'n/a'}</div>
                <div class="views"><h3>Page visits last ${this.viewDateRange} days:</h3>
                <div class="dateRangeSelect">
                  <button ?disabled="${this.viewDateRange == 7}" @click="${e => this.dateRangeUpdated(7)}">7</button>
                  <button ?disabled="${this.viewDateRange == 30}" @click="${e => this.dateRangeUpdated(30)}">30</button>
                  <button ?disabled="${this.viewDateRange == 180}" @click="${e => this.dateRangeUpdated(180)}">180</button>
                  <button ?disabled="${this.viewDateRange == 365}" @click="${e => this.dateRangeUpdated(365)}">365</button>
                </div>
                <div class="graph">
                        ${this.addonVisits.map(
                          (num,i) => html`<div class="bar" style="height: ${(num / max) * 100 + 0.1}%;" title="${this.dateOf(this.viewDateRange-i)}: ${num}"></div>`
                        )}
                      </div>
                      <div class="legend">${this.dateOf(this.viewDateRange)}<span class="total">Total ${this.addonVisits.reduce((a, b) => a + b, 0)}</span>Today</div>
               </div>

              <div class="countries">
                <h3>Visit by Countries</h3>
                    <ul>
                    ${!this.addonCountries.size ? html`<li>No country data available</li>` :
                      Array.from(this.addonCountries.entries())
                        .sort((a, b) => b[1] - a[1])
                        .map(([country, count]) => html`<li>${country}: ${count}</li>`)}
                    </ul>
             </div>
            </section>
        </section>
      `;
    }

    dateOf(daysBefore: number) {
        const date = new Date();
        date.setDate(date.getDate() - daysBefore);
        return date.toISOString().slice(0, 10); // Returns YYYY-MM-DD format
    }


   firstUpdated() {
      if (this.addon) {
        getAddonInstallCountExact(this.addon.urlIdentifier, {mute: true}).then(v =>  {this.addonInstallCount = v;});
        getAddonMavenDownloadCount(this.addon.urlIdentifier, {mute: true}).then(v =>  {this.addonMavenDownloadCount = v;});
        this.dateRangeUpdated(this.viewDateRange);
      }
    }

   dateRangeUpdated(dateRange: number) {
       this.viewDateRange = dateRange;
      if (this.addon) {
        getVisits(this.viewDateRange, this.addon.urlIdentifier, {mute: true}).then(v =>  {this.addonVisits = v;});
        getCountries(this.viewDateRange, this.addon.urlIdentifier, {mute: true}).then(v =>  {this.addonCountries = new Map(Object.entries(v))});
      }
    }


  searchByAuthor() {
      if (this.addon) {
          const authorHref = router.urlForPath('user-stats/:user', {user: this.addon.author })
          window.location.href = authorHref;
      }
  }

  versionOrder(a: AddonVersion, b: AddonVersion): number {
    return a.date < b.date ? 1:-1;
  }

  getLatestVersion(): AddonVersion | undefined {
    return this.addon?.versions.sort(this.versionOrder)[0];
  }
  getGitHubLink() {
    const link = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/github.com\/[-_\w\d]+\/[-_\w\d]+(.git)?/)
    );
    return link ? link.href : null;
  }
}