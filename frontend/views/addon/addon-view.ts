import '@vaadin/vaadin-text-field';
import '@vaadin/vaadin-grid/vaadin-grid';
import '@vaadin/vaadin-select';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { View } from '../view';
import { getAddon } from 'Frontend/generated/AddonEndpoint';
import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';
import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import DomPurify from 'dompurify';
import { marked } from 'marked';
import { highlight, languages } from 'prismjs';
import 'prismjs/themes/prism.css';
import 'prismjs/components/prism-java';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-typescript';
import 'prismjs/components/prism-css';

@customElement('addon-view')
export class AddonView extends View implements BeforeEnterObserver {
  @state()
  private addon?: Addon;

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
    if (!this.addon) {
      return html`Loading...`;
    }

    return html`
      <div class="flex gap-l">
        <div class="flex flex-col gap-xs flex-wrap">
            <h1>${this.addon.name}</h1>
            <div>${this.addon.author} last updated ${this.addon.lastUpdated}</div>
                      <div class="flex gap-xs flex-wrap">
                        ${this.addon.tags.map(
                          (tag) =>
                            html`
                              <vaadin-button
                                @click=${() => this.searchByTag(tag)}
                                theme="small">
                                ${tag}
                              </vaadin-button>
                            `
                        )}
                      </div>
            <p>${this.addon.summary}</p>
            ${unsafeHTML(DomPurify.sanitize(marked.parse(this.addon.description)))}
          </div>
          <div class="bg-primary-10 shadow-xs row-span-2 m-s p-m">
              <h3>Install</h3>
              <p>
                  Add-on version <br />
                  <select label="Add-on version">
                      <option>Version 2.0.6</option>
                      <option>Version 2.0.5</option>
                      <option>Version 2.0.4</option>
                      <option>Version 1.0.5</option>
                  </select>
              </p>
              <p>
                <a class="size-xs" href="">Link to this version</a>
              </p>
              <p>
                Experimental<br />
                Released 15 December 2021<br />
                Apache License 2.0<br />
              </p>
              <h3>Framework support</h3>
              <p>
                Vaadin platform 20+
              </p>
              <h3>Browser compatibility</h3>
              <p>
              Browser independent
              </p>
              <h3>Install with</h3>
              <p>
                <vaadin-button>Log in to install</vaadin-button>
              </p>
              <h3>Release notes - Version 2.0.6</h3>
              <p>
              Fixed bugs in rendering.
              </p>
          </div>
      </div>
    `;
  }

  async onBeforeEnter(location: RouterLocation) {
    const urlIdentifier = location.params.addon as string;
    this.addon = await getAddon(urlIdentifier);
  }

  searchByTag(tag: string) {
      window.location.href = "../?q=tag:"+tag;
    }
}
