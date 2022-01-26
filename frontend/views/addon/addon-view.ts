import './addon-view.css';
import '../../components/addon-icon';
import '../../components/rating-stars';
import './install-tabsheet';
import './highlight-carousel';
import './feature-matrix';
import './contributors';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import '@vaadin/vaadin-select/src/vaadin-select';
import { html, nothing, render } from 'lit';
import { customElement, state } from 'lit/decorators.js';
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
import '@vaadin/avatar/src/vaadin-avatar';
import { appStore } from 'Frontend/stores/app-store';
import { searchStore } from 'Frontend/views/search/search-store';
import { router } from '../../index';
import { disqusReset } from "../disqus";

import { SearchEndpoint } from 'Frontend/generated/endpoints';
import Matrix from 'Frontend/generated/org/vaadin/directory/endpoint/search/Matrix';

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {

  @state()
  private addon?: Addon;

  @state()
  private version?: AddonVersion;

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

    const params = new URLSearchParams();
    params.set('q', searchStore.query);

    return html`
      <a href="${router.baseUrl}?${params}" class="back-to-search">← Back</a>
      <section class="main">

        <addon-icon src="${this.addon.icon}"></addon-icon>
        <h2 class="addon-name">${this.addon.name}</h2>
        <p class="addon-summary">${this.addon.summary}</p>

        <section class="meta">
          <section class="author">
            <h3>Author</h3>
            <button @click="${this.searchByAuthor}">
              <vaadin-avatar .img="https://vaadin.com/static/portrait/initials/JD" name="John Doe"></vaadin-avatar>
              ${this.addon.author}
            </button>
          </section>

          ${this.getGitHubLink() ? html`
            <section class="contributors">
              <h3>Contributors</h3>
              <github-contributors repositoryUrl="${this.getGitHubLink()}"></github-contributors>
            </section>
          `:nothing}

          <section class="rating">
            <h3>Rating</h3>
            <rating-stars .rating="${this.addon.rating}" .ratingCount="${this.addon.ratingCount}"></rating-stars>
          </section>

          ${this.addon.links.length > 0 ? html`
            <section class="links">
              <h3>Links</h3>
              ${this.getHighlightLinks()}
            </section>
          `:nothing}
        </section>

        <highlight-carousel class="highlights" .addon=${this.addon}></highlight-carousel>

        ${unsafeHTML(
          DomPurify.sanitize(marked.parse(this.addon.description || ""))
        )}

        ${this.addon.codeSamples && this.addon.codeSamples.length > 0 ?
          html`<h2>Sample code</h2>
            ${this.addon.codeSamples.map((s) => html`
              <pre class="sample-code ${s?.type}">${s?.code}</pre>
            `)}`
          :
          html``
        }

        <h3>Links</h3>
        <ul>
          ${this.addon.links.map((l) => html`
            <li><a href="${l.href}">${l.name}</a></li>
          `)}
        </ul>

        <h3>Compatibility</h3>
        <feature-matrix .matrix="${this.compatibility}" class="compatibility-matrix"></feature-matrix>

        <section class="footer">
          ${this.addon.tags.map((tag) => html`
            <button class="tag" @click=${() => this.searchByTag(tag)}>${tag}</button>
          `)}
          <p class="updated">Last updated: ${this.addon.lastUpdated}</p>
        </section>
      </section>

      <section class="versions">
        <header>
          <h3>Version</h3>
          <vaadin-select
            theme="version-select"
            value="${this.version?.name}"
            @value-changed=${this.versionChange}
            .renderer="${guard(
              [],
              () => (elem: HTMLElement) =>
                render(
                  html`
                    <vaadin-list-box>
                      ${this.addon?.versions.sort(this.versionOrder).map((v) => html`
                        <vaadin-item value="${v.name}" label="${v.name}">
                          <span>${v.name}</span>
                          <span class="maturity ${v.maturity.toLowerCase()}">${v.maturity.toLowerCase()}</span>
                          <span class="release-date">${v.date}</span>
                        </vaadin-item>
                      `)}
                    </vaadin-list-box>
                  `,
                  elem
                )
            )}">
          </vaadin-select>
          <install-tabsheet .addon="${this.addon}" .version="${this.version}"></install-tabsheet>
        </header>

        <section class="release-notes">
          ${unsafeHTML(
            DomPurify.sanitize(marked.parse(this.version.releaseNotes || ""))
          )}
        </section>

        <dl class="details">
          <dt>Released</dt><dd>${this.version?.date}</dd>
          <dt>Maturity</dt><dd>${this.version?.maturity}</dd>
          <dt>License</dt><dd>${this.version?.license}</dd>
        </dl>

        <h4>Compatibility</h4>
        <dl class="compatibility">
          <dt>Framework</dt>
          ${this.version?.compatibility.map((compat) => html`
              <dd>${compat}</dd>
          `)}
          ${this.getAlsoSupported(this.addon, this.version)}

          <dt>Browser</dt>
          ${this.version?.browserCompatibility.length > 0 ? this.version?.browserCompatibility.map(
            (compat) => html`<dd>${compat}</dd>`
          ) : html`<dd>N/A</dd>` }
        </dl>
      </section>
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
      return versions.reverse().map((c) => html`
        <dd>${c} in <a href="${router.urlForPath('addon/:addon/:version?', {addon: addon.urlIdentifier, version: supportedByOthers.get(c)+'' })}">${supportedByOthers.get(c)}</a></dd>
      `);
    } else {
      return nothing;
    }
  }

  getGitHubLink() {
    const link = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/github.com\/[-_\w\d]+\/[-_\w\d]+(.git)?/)
    );
    return link ? link.href : null;
  }

  getHighlightLinks() {
    const gitHubLink = this.getGitHubLink();
    const demoLink = this.addon?.links.find((link) => link.name.match(/demo/i));
    const kofiLink = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/ko-fi.com\/[-_\w\d]+/)
    );

    return html` <ul>
      ${demoLink
        ? html` <li>
            <a href="${demoLink.href}" target="_blank" noopener>Demo</a>
          </li>`
        : nothing}
      ${gitHubLink
        ? html` <li>
            <a href="${gitHubLink}" target="_blank" noopener>GitHub</a>
          </li>`
        : nothing}
      ${kofiLink
        ? html` <li>
            <a href="${kofiLink.href}" target="_blank" noopener>Tip me</a>
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

  searchByAuthor() {
    if (this.addon) {
      this.searchByUser(this.addon.author);
    }
  }

  searchByTag(tag: string) {
    window.location.href = router.baseUrl + '?q=tag:' + tag;
  }

  searchByVersion(v: string) {
    window.location.href = router.baseUrl + '?q=v:' + v;
  }

  searchByUser(user: string) {
    window.location.href = router.baseUrl + '?q=author:' + user;
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
