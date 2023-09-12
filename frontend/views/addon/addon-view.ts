import './addon-view.css';
import '../../components/addon-icon';
import { RatingStars, RatingEvent } from '../../components/rating-stars';
import './install-tabsheet';
import './highlight-carousel';
import './feature-matrix';
import './contributors';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { getAddon, getUserRating, setUserRating } from 'Frontend/generated/AddonEndpoint';
import '@vaadin/vaadin-select/src/vaadin-select';
import { html, nothing, render } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { View, AddonJsonLd } from '../view';
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
import { iframeResizer } from 'iframe-resizer';

import { SearchEndpoint } from 'Frontend/generated/endpoints';

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {

  @state()
  private addon?: Addon;

  @state()
  private version?: AddonVersion;

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
      return html`<section class="main"><h3>Hmm... That add-on was not found.</h3>
      <div>${appStore.appUrl? html`<a href="${appStore.appUrl}">Try search for it instead.</a>`:''}</div></section>`;
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
              <vaadin-avatar .img="${this.addon.authorImage}" name="${this.addon.author}"></vaadin-avatar>
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
            <rating-stars
              id="rating-stars"
              @rating=${this.addRating}
              @mouseover=${this.checkUserStatus}
              ?readonly="${!this.isAuthenticated()}"
              userrating="0"
              avgrating="${this.addon.rating}"
              ratingcount="${this.addon.ratingCount}">
            </rating-stars>
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

        <h3  id="links">Links</h3>
        <ul>
          ${this.addon.links.map((l) => html`
            <li><a href="${l.href}" router-ignore>${l.name}</a></li>
          `)}
        </ul>

        <h3 id="compatibility">Compatibility</h3>
        <feature-matrix .addon="${this.addon.urlIdentifier}" class="compatibility-matrix"></feature-matrix>

        <section class="footer">
          ${this.addon.tags.map((tag) => html`
            <button class="tag" @click=${() => this.searchByTag(tag)}>${tag}</button>
          `)}
          <p class="updated">Last updated: ${this.addon.lastUpdated}</p>
        </section>

        <section id="discussion" class="discussion">
          <p>
            <b>Was this helpful? Need more help?</b><br />Leave a comment or a question below. You can also join
            the <a href="https://discord.gg/MYFq5RTbBn" rel="noopened">chat on Discord</a> or
            <a href="https://stackoverflow.com/questions/tagged/vaadin" rel="noopened">ask questions on StackOverflow</a>.
          </p>
          <iframe id="discussion-iframe" src="${this.getDiscussionLink()}"></iframe>
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

  getDiscussionLink() {

    let iframeSrc = window.location.hostname == 'preview.vaadin.com'
      ? 'https://preview.vaadin.com'
      : 'https://vaadin.com';

      iframeSrc += `/vaadincom/discussion-service/embed.html?root=DIRECTORY&id=${this.addon?.discussionId}&url=${encodeURI(document.location.pathname)}&name=${encodeURI(
        ''+this.addon?.name)}`;  

    return iframeSrc;

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
        <dd>${c} in <a href="${router.urlForPath('component/:addon/:version?', {addon: addon.urlIdentifier, version: supportedByOthers.get(c)+'' })}">${supportedByOthers.get(c)}</a></dd>
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
    const editLink = this.addon?.links.find((link) =>
      link.href.match(/http(s)?:\/\/vaadin.com\/directory\/component\/edit\/[-_\w\d]+/)
    );

    return html` <ul>
      ${demoLink
        ? html` <li>
            <a href="${demoLink.href}" target="_blank" router-ignore noopener>Demo</a>
          </li>`
        : nothing}
      ${gitHubLink
        ? html` <li>
            <a href="${gitHubLink}" target="_blank" router-ignore noopener>GitHub</a>
          </li>`
        : nothing}
      ${kofiLink
        ? html` <li>
            <a href="${kofiLink.href}" target="_blank" router-ignore noopener>Tip me</a>
          </li>`
        : nothing}
      ${editLink
          ? html` <li>
              <a class="edit" href="${editLink.href}" target="_blank" router-ignore noopener>Edit</a>
            </li>`
          : nothing}
    </ul>`;
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
      appStore.currentViewTitle = this.addon.name;
      this.updatePageMetadata();
    }
  }

  firstUpdated() {
    if (this.addon) {
      this.updateUserRating();
    }
    iframeResizer({ log: false }, '#discussion-iframe');
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
    if (user) {
      window.location.href = router.baseUrl + '?q=author:' + user.replace(/ /g,'_');
    } else {
      window.location.href = router.baseUrl;
    }

  }

  checkUserStatus() {
    const stars = this.renderRoot.querySelector('#rating-stars') as RatingStars;
    if (!stars) return;
    if (this.isAuthenticated() && stars.readonly) {
      // Login status has been changed since first render. Update user rating.
      stars.readonly = false;
      this.updateUserRating();
    }
  }

  addRating(e: RatingEvent) {
    (e.target as RatingStars).userrating = e.rating;
    (e.target as RatingStars).tooltip = 'Click to rate again';
    setUserRating(this.addon?.urlIdentifier, e.rating, this.getCurrentUserId());
  }

  async updateUserRating() {
    const stars = this.renderRoot.querySelector('#rating-stars') as RatingStars;
    if (!stars) return;

    stars.readonly = !this.isAuthenticated();
    if (stars.readonly) {
      stars.tooltip = "Login to rate this addon";
      return;
    }

    stars.userrating = 0;
    stars.tooltip = "Click to rate this addon";
    stars.userrating = await getUserRating(this.addon?.urlIdentifier, this.getCurrentUserId());
  }

  updatePageMetadata(): void {

    // Construct canonical URL
    var canonicalUrl = router.urlForPath('component/:addon', {addon: this.addon!.urlIdentifier});
    canonicalUrl = canonicalUrl.startsWith("/directory/")? canonicalUrl.substring(11) : canonicalUrl;
    canonicalUrl = (canonicalUrl.startsWith("/") && appStore.appUrl.endsWith("/"))?
          appStore.appUrl + canonicalUrl.substring(1) :
          appStore.appUrl + canonicalUrl;

    // Create search metadata
    const addonMetadata = new AddonJsonLd(
      this.addon!.name,
      canonicalUrl,
      this.addon!.author,
      this.addon!.icon,
      null, // TODO: Support screenshots
      this.addon!.lastUpdated,
      this.addon!.rating,
      this.addon!.ratingCount
    );
    addonMetadata.appendOrReplaceToHead();

    // Update Canonical URL
    const canonical = document.head.querySelector('link[rel="canonical"]') as HTMLElement; 
    if (canonical) canonical.setAttribute("href",canonicalUrl);

    // Update Twitter metadata
    const title = document.head.querySelector('meta[name="twitter:title"]') as HTMLElement; 
    const summary = document.head.querySelector('meta[name="twitter:description"]') as HTMLElement; 
    const icon = document.head.querySelector('meta[name="twitter:image"]') as HTMLElement; 
    if (title) title.setAttribute("content",this.addon!.name);
    if (summary) summary.setAttribute("content",this.addon!.summary);
    if (icon) icon.setAttribute("content",this.addon!.icon ? this.addon!.icon : "https://vaadin.com/images/directory/addon-icon-default.png");
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

}

