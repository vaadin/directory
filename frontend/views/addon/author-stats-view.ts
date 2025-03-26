import './addon-view.css';
import '../../components/addon-icon';
import '@vaadin/avatar/src/vaadin-avatar';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import {
  columnBodyRenderer,
  columnFooterRenderer,
  columnHeaderRenderer,
} from '@vaadin/grid/lit.js';
import { RatingStars, RatingEvent } from '../../components/rating-stars';
import {getAddonsByUser,getUserInfo } from 'Frontend/generated/SearchEndpoint';
import StatsResults from 'Frontend/generated/org/vaadin/directory/endpoint/search/StatsResults';
import UserInfo from 'Frontend/generated/org/vaadin/directory/endpoint/search/UserInfo';
import { getAddon, getLatestVersion, getUserRating, getAddonInstallCountExact, getAddonMavenDownloadCount, getVisits, getCountries } from 'Frontend/generated/AddonEndpoint';
import { html, css, nothing, render } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { guard } from 'lit/directives/guard.js';
import { appStore } from 'Frontend/stores/app-store';
import { router } from '../../index';
import { View } from '../view';

@customElement('author-stats-view')
export class StatsView extends View implements BeforeEnterObserver {

  @state()
  private user?: string;

  @state()
  private userInfo: UserInfo;

  @state()
  private addons?: StatsResults[] = [];

  @state()
  private userInstallCount: number = 0;

  @state()
  private viewDateRange: number = 30;

  constructor() {
    super();
  }

  updatePageMetadata(): void {
  }


    async onBeforeEnter(location: RouterLocation) {
      const userId = location.params.user as string;
      this.user = userId;
      this.userInfo = await getUserInfo(this.user);
      if (this.userInfo) {
        this.addons = await getAddonsByUser(this.user);
        if (this.addons) {
             // Fetch detailed stats for each addon
             const promises = this.addons.map(async (addon) => {
               const installCount = await getAddonInstallCountExact(addon.urlIdentifier);
               const visits = await getVisits(this.viewDateRange, addon.urlIdentifier);

               // Update totals
               this.userInstallCount += installCount || 0;

               // Set the properties on the addon object for grid display
               addon.totalInstalls = installCount || 0;
               addon.totalVisits = visits || 0;
             });

             await Promise.all(promises);
           }
        }
        appStore.setCurrentViewTitle("Stats for "+this.userInfo.fullName);
      }

    private ratingRenderer: GridColumnBodyLitRenderer<StatsResults> = (addon) => html`<rating-stars avgrating="${addon.avgRating.toFixed(2)}" minNumberOfRatings="1" ratingcount="${addon.ratingCount}" readonly="true"></rating-stars>`;

    private linkRenderer: GridColumnBodyLitRenderer<StatsResults> = (addon) => html`<a href="${router.urlForPath('component/:addon/stats', { addon: addon.urlIdentifier })}">${addon.name}</a>`;

    private visitRenderer: GridColumnBodyLitRenderer<StatsResults> = (addon) => {
          if (!addon.totalVisits) return html`n/a`;

          // Create a simple sparkline with div bars
          const maxValue = Math.max(...addon.totalVisits);
          const normalizedValues = addon.totalVisits.map(val => (val / maxValue) * 100);
          return html`
            <div class="sparkline">
              ${normalizedValues.map(value => html`
                <div class="sparkline-bar" style="height: ${value}%"></div>
              `)}
              <span class="sparkline-value">${addon.totalVisits.reduce((a, b) => a + b, 0)}</span>
            </div>
          `;
          };

    render() {
        if (!this.userInfo || this.addons.length == 0) {
          return html`<section class="main"><h3>Hmm... That user was not found.</h3></div></section>`;
        }

    return html`
            <style>
              .sparkline {
                display: flex;
                align-items: flex-end;
                height: 20pt;
                gap: 1pt;
              }
              .sparkline-bar {
                width: 3pt;
                background-color: var(--blue-500);
                min-height: 1pt;
              }
              .sparkline-value {
                margin-left: 8pt;
                align-self: center;
              }
            </style>

            <section class="main">
                <h2>Addons by ${this.userInfo.fullName} (${this.userInfo.screenName})</h2>
                <span class="userTotal">Total installations: ${this.userInstallCount}</span>
                <vaadin-grid .items="${this.addons}" all-rows-visible>
                    <vaadin-grid-sort-column
                        path="name"
                        flex-grow="0"
                        auto-width
                        ${columnBodyRenderer(this.linkRenderer, [])}
                        header="Addon">
                    </vaadin-grid-sort-column>
                    <vaadin-grid-sort-column
                        path="summary"
                        header="Summary">
                    </vaadin-grid-sort-column>
                    <vaadin-grid-sort-column
                        path="totalInstalls"
                        flex-grow="0"
                        header="Installs">
                    </vaadin-grid-sort-column>
                    <vaadin-grid-sort-column
                        path="totalVisits"
                        flex-grow="0"
                        auto-width
                        ${columnBodyRenderer(this.visitRenderer, [])}
                        header="Visits">
                    </vaadin-grid-sort-column>
                    <vaadin-grid-sort-column
                        path="ratingCount"
                        flex-grow="0"
                        auto-width
                        ${columnBodyRenderer(this.ratingRenderer, [])}
                        header="Rating">
                    </vaadin-grid-sort-column>
                    <vaadin-grid-sort-column
                        path="avgRating"
                        flex-grow="0"
                        auto-width
                        ${columnBodyRenderer(addon => addon.avgRating.toFixed(2), [])}
                        header="Avg Ratings">
                    </vaadin-grid-sort-column>
                </vaadin-grid>
            </section>
            `;
    }


   firstUpdated() {

   }


}