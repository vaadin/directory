import './install-tabsheet';
import './highlight-carousel';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import '@vaadin/vaadin-select';
import { html, render } from 'lit';
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

const OFFICIAL_TAG = "Sponsored";

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {
  @state()
  private addon?: Addon;

  @state()
  private version?: AddonVersion;

  // TODO: User information missing
  @state()
  private user?: Object  = {};

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
        <div class="flex flex-col flex-wrap">
          <img style="width: 128px; height: 128px" src="${this.addon.icon}">
            <h1>${this.addon.name}</h1>
            <span class="text-m">${this.addon.rating > 0 ?
                    (this.addon.rating < 5 ?
                      '★️'.repeat(this.addon.rating) + '☆'.repeat(5-this.addon.rating):
                      '★️'.repeat(this.addon.rating) ) :
                    '☆☆☆☆☆' }</span>
            <div class="updated">Updated on ${this.addon.lastUpdated}</div>
            <div class="user">By ${this.addon.author}</div>
            <div class="tags">
                ${this.addon.tags.map(
                  (tag) =>
                    html`
                      <vaadin-button style="cursor:pointer;"
                        @click=${() => this.searchByTag(tag)}
                        theme="badge pill">
                        ${tag}
                        ${OFFICIAL_TAG === tag ? html`<span class="m-xs"><vaadin-icon icon="vaadin:vaadin-v"></vaadin-icon></span>`:''}
                      </vaadin-button>
                    `
                )}
            </div>
            <p>${this.addon.summary}</p>
            <highlight-carousel .addon=${this.addon}></highlight-carousel>
            <p>
            ${unsafeHTML(DomPurify.sanitize(marked.parse(this.addon.description)))}
            </p>
            <h2>Links</h2>
            <ul>
            ${this.addon.links.map((l) => html`<li><a href="${l.href}">${l.name}</a></li> `)}
            </ul>
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
                              ${this.addon?.versions.map(
                                (v) => html`
                                <vaadin-item value="${v.name}" label="${v.name}">
                                <div style="display: flex; align-items: center;">
                                <span class="font-bold m-xs">${v.name}</span>
                                <span class="text-xs font-light">${v.date},
                                <span class="${v.maturity == 'STABLE'? 'bg-success text-success-contrast' : 'bg-base'} text-2xs font-light p-xs rounded-m">${v.maturity}</span>
                                </div>
                                </vaadin-item>
                                `
                              )}
                            </vaadin-list-box>
                          `,
                          elem
                        )
                    )}"
                  ></vaadin-select>
                  <br /><a class="text-xs" href="${location.href + this.version?.name}">Link to this version</a>
                </p>
                <p>
                  ${this.user ?
                  html`<install-tabsheet .version=${this.version}></install-tabsheet>`:
                  html`<vaadin-button>Log in to install</vaadin-button>`}
                </p>
                <p>
                ${unsafeHTML(DomPurify.sanitize(marked.parse(this.version.releaseNotes)))}
                </p>
                <hr />
                <p class="text-s">
                  ${this.version?.date} <br />
                  ${this.version?.maturity}<br />
                  ${this.version?.license}<br />
                </p>
                <hr />
                <h3>Framework support</h3>
                <p>
                  ${this.version?.compatibility.map((compat) =>
                    html`${compat}<br />`
                  )}
                </p>
                <hr />
                <h3>Browser compatibility</h3>
                <p>
                  ${this.version?.browserCompatibility.map((compat) =>
                    html`${compat}<br />`
                  )}
                </p>
          </div>
      </div>
    `;
  }

  async onBeforeEnter(location: RouterLocation) {
    const urlIdentifier = location.params.addon as string;
    this.addon = await getAddon(urlIdentifier);
    if (this.addon) this.version = this.addon.versions[0];
  }

  searchByTag(tag: string) {
      window.location.href = "../?q=tag:"+tag;
    }

  versionChange(e: CustomEvent) {
      if (e && e.detail && e.detail && e.detail.value) {
          const found = this.addon?.versions.find((v: AddonVersion) => v.name == e.detail.value )
          this.version =  found ? found : this.addon?.versions[0];
      } else {
          this.version = this.addon?.versions[0];
      }
  }

}
