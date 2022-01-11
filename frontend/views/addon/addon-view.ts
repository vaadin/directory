import './install-tabsheet';
import './highlight-carousel';
import './feature-matrix';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import '@vaadin/vaadin-select';
import { html, nothing, render } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { View } from '../view';
import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { guard } from 'lit/directives/guard.js';
import DomPurify from 'dompurify';
import { marked } from 'marked';
import { highlight, languages } from 'prismjs';
import 'prismjs/themes/prism.css';
import 'prismjs/components/prism-java';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-typescript';
import 'prismjs/components/prism-css';
import '@vaadin/icon';
import '@vaadin/icons';
import { appStore } from 'Frontend/stores/app-store';
import { router } from '../../index';
import { disqusReset } from "../disqus"

import { SearchEndpoint } from 'Frontend/generated/endpoints';
import Matrix from 'Frontend/generated/org/vaadin/directory/endpoint/search/Matrix';

const OFFICIAL_TAG = 'Sponsored';

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {

  @state()
  private addon?: Addon;

  @state()
  private version?: AddonVersion;

  // TODO: User information missing
  @state()
  private user?: Object = {};

  @state()
  private compatibility?: Matrix;

  constructor() {
    super();
    marked.setOptions({
      highlight: (code, lang) => {
        if (languages[lang]) {
          return highlight(code, languages[lang], lang);
        } else {
          return code;
        }
      },
    });
  }

  render() {
    if (!this.addon || !this.version) {
      return html`Loading...`;
    }

    return html`
      <div class="flex gap-l">
        <div class="flex flex-col flex-auto flex-wrap">
          <img style="width: 128px; height: 128px" src="${this.addon.icon}" />
          <h1>${this.addon.name}</h1>
          <div class="flex">
            <div class="flex flex-col flex-auto flex-wrap">
              <span>${this.addon.rating > 0
                  ? this.addon.rating < 5
                    ? '★️'.repeat(this.addon.rating) +
                      '☆'.repeat(5 - this.addon.rating)
                    : '★️'.repeat(this.addon.rating)
                  : '☆☆☆☆☆'} (${this.addon.ratingCount})</span>
              <div class="updated">Updated on ${this.addon.lastUpdated}</div>
              <div class="user">By ${this.addon.author}</div>
            </div>
            <div class="highlight-links">${this.getHighlightLinks()}</div>
          </div>
          <div class="tags">
            ${this.addon.tags.map(
              (tag) =>
                html`
                  <vaadin-button
                    style="cursor:pointer;"
                    @click=${() => this.searchByTag(tag)}
                    theme="badge pill">
                    ${tag}
                    ${OFFICIAL_TAG === tag
                      ? html`<span class="m-xs">
                          <vaadin-icon icon="vaadin:vaadin-v"></vaadin-icon>
                        </span>`
                      : ''}
                  </vaadin-button>
                `
            )}
          </div>
          <p>${this.addon.summary}</p>
          <highlight-carousel .addon=${this.addon}></highlight-carousel>
          <p>
            ${unsafeHTML(
              DomPurify.sanitize(marked.parse(this.addon.description || ""))
            )}
          </p>
          <h2>Links</h2>
          <ul>
            ${this.addon.links.map(
              (l) => html`<li><a href="${l.href}">${l.name}</a></li> `
            )}
          </ul>
          <h2>Full Compatibility</h2>
          <div class="compatibility-matrix"><feature-matrix .matrix=${this.compatibility}></feature-matrix></div>
        </div>
        <div class="side-panel">
          <h3>Install</h3>
          <p>
            <vaadin-select
              value="${this.version?.name}"
              @value-changed=${this.versionChange}
              .renderer="${guard(
                [],
                () => (elem: HTMLElement) =>
                  render(
                    html`
                      <vaadin-list-box>
                        ${this.addon?.versions.sort(this.versionOrder).map(
                          (v) => html`
                                <vaadin-item value="${v.name}" label="${
                            v.name
                          }">
                                <div style="display: flex; align-items: center;">
                                <span class="font-bold">${v.name}</span>
                                <span class="${
                                  v.maturity == 'STABLE'
                                    ? 'bg-success text-success-contrast'
                                    : 'bg-base'
                                } text-2xs font-light m-xs p-xs rounded-l">
                                ${v.maturity}
                                 </span>
                                 <span class="text-2xs font-light"> ${v.date}
                                </div>
                                </vaadin-item>
                                `
                        )}
                      </vaadin-list-box>
                    `,
                    elem
                  )
              )}"></vaadin-select>
            <br /><a
              class="text-xs"
              href="${router.urlForPath('addon/:addon/:version?', {addon: this.addon?.urlIdentifier, version: this.version?.name })}"
              >Link to this version</a
            >
          </p>
          <p>
            ${this.user
              ? html`<install-tabsheet
                  .version=${this.version}></install-tabsheet>`
              : html`<vaadin-button>Log in to install</vaadin-button>`}
          </p>
          <p>
            ${unsafeHTML(
              DomPurify.sanitize(marked.parse(this.version.releaseNotes || ""))
            )}
          </p>
          <hr />
          <p class="text-s">
            <span>Released: ${this.version?.date}</span> <br />
            <span>Maturity: ${this.version?.maturity}</span><br />
            <span>License: ${this.version?.license}</span><br />
          </p>
          <hr />
          <h3>Framework support</h3>
          <p>
            ${this.version?.compatibility.map(
              (compat) => html`${compat}<br />`
            )}
            ${this.getAlsoSupported(this.addon, this.version)}

          </p>
          <hr />
          <h3>Browser compatibility</h3>
          <p>
            ${this.version?.browserCompatibility.length > 0 ? this.version?.browserCompatibility.map(
              (compat) => html`${compat}<br />`
            ) : html`(no browser information provided)` }
          </p>
          <p>
              <a href="${location.href}#discussions"><span class="fa far fa-lightbulb"></span> Report browser compatibility.</a>
          </p>
        </div>
      </div>
    `;
  }

  versionOrder(a: AddonVersion, b: AddonVersion): number {
    return a.date < b.date ? 1:-1;
  }

  getLatestVersion(): AddonVersion | undefined {
    return this.addon?.versions.sort(this.versionOrder)[0];
  }

  getAlsoSupported(addon: Addon, currentVersion: AddonVersion) {
    // Collect supported data
    const supportedByOthers = new Map<string, string>();
    addon.versions.reverse().forEach(v => // reverse to keep the latest one
      v.compatibility.forEach((c:string) => supportedByOthers.set(c, v.name))
    );
    // Delete versions supported byt this
    currentVersion.compatibility
      .forEach(c => supportedByOthers.delete(c));

    const versions = Array.from(supportedByOthers.keys());
    if (versions.length > 0) {
     return html`
        <p>
        <b>Also supported:</b><br />
        ${versions.reverse().map(
          (c) =>
          html`${c} <a href="${router.urlForPath('addon/:addon/:version?', {addon: addon.urlIdentifier, version: supportedByOthers.get(c)+'' })}"> in ${supportedByOthers.get(c)}</a><br />`
        )}
        </p>
        <p>
            <a href="${location.href}#discussions"><span class="fa far fa-lightbulb"></span> Suggest support for a new version.</a>
        </p>
       `;
    } else {
        return html`
        <p>
            <a href="${location.href}#discussions"><span class="fa far fa-lightbulb"></span> Suggest support for a new version.</a>
        </p>
        `;
    }
  }

  getHighlightLinks() {
    const gitHubLink = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/github.com\/[-_\w\d]+\/[-_\w\d]+(.git)?/)
    );
    const demoLink = this.addon?.links.find((link) => link.name.match(/demo/i));
    const kofiLink = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/ko-fi.com\/[-_\w\d]+/)
    );

    const icons = ["cat", "cloud", "couch", "compass", "flask", "flushed", "gift", "glass-martini", "hand-peace", "heart", "poo"];
    const icon: string = icons[Math.floor(Math.random() * icons.length)];

    return html` <ul>
      ${demoLink
        ? html` <li>
            <a href=${demoLink.href} target="_blank" noopener>
              <i class="text-xl fas fa-${icon}"></i> Demo
            </a>
          </li>`
        : nothing}
      ${gitHubLink
        ? html` <li>
            <a href=${gitHubLink.href} target="_blank" noopener>
              <i class="text-xl fab fa-github"></i> GitHub
            </a>
          </li>`
        : nothing}
      ${kofiLink
        ? html` <li>
            <a href=${kofiLink.href} target="_blank" noopener>
              <i class="text-xl fas fa-coffee"></i> Tip me
            </a>
          </li>`
        : nothing}
    </ul>`;
  }

  async onBeforeEnter(location: RouterLocation) {
    const urlIdentifier = location.params.addon as string;
    const urlVersion = location.params.version as string;
    this.addon = await getAddon(urlIdentifier);
    if (this.addon) {
      if (urlVersion) {
        const found = this.addon?.versions.find(
          (v: AddonVersion) => v.name == urlVersion
        );
        this.version = found ? found : this.getLatestVersion();
      } else {
        this.version = this.getLatestVersion();
      }
      appStore.currentViewTitle = this.addon.name;
    }
  }

  firstUpdated() {
    // Reset discuss thread
    if (this.addon) {
      disqusReset(this.addon.urlIdentifier,
        "https://directory4.demo.vaadin.com"+router.urlForPath('addon/:addon/:version?', {addon: this.addon.urlIdentifier }),
        this.addon.name, true);
      this.fetchCompatibility();
    }
  }

  searchByTag(tag: string) {
    window.location.href = '../?q=tag:' + tag;
  }

  searchByVersion(v: string) {
    window.location.href = '../?q=v:' + v;
  }

  searchByUser(user: string) {
    window.location.href = '../?q=author:' + user;
  }


  versionChange(e: CustomEvent) {
    if (e && e.detail && e.detail && e.detail.value) {
      const found = this.addon?.versions.find(
        (v: AddonVersion) => v.name == e.detail.value
      );
      this.version = found ? found : this.getLatestVersion();
    } else {
      this.version = this.getLatestVersion();
    }
  }

    async fetchCompatibility() {
      this.compatibility =
        await SearchEndpoint.getCompatibility(this.addon?.urlIdentifier);
   }

}
