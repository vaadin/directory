import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import '@vaadin/vaadin-tabs';
import '@vaadin/vaadin-lumo-styles/typography';
import '@vaadin/vaadin-lumo-styles/sizing';
import '@vaadin/vaadin-lumo-styles/spacing';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('install-tabsheet')
export class InstallTabSheet extends View {

  @property({ attribute: false })
  version?: AddonVersion;

  constructor() {
    super();
    this.tabChanged(null);
  }

  render() {
    if (!this.version) {
      return html`skeletor!`;
    }
    return html`
        <vaadin-tabs id="tabs" @selected-changed="${this.tabChanged}">
          ${Object.keys(this.version?.installs).map((key) => html` <vaadin-tab>${key}</vaadin-tab> `)}
        </vaadin-tabs>
        <div id="sheets" class="rounded-m">
          ${Object.keys(this.version?.installs).map((key) => html`
            <div class="">
            ${key == "Zip" ? html`<div class="p-l a-c"><span theme="badge pill"><a href=" https://static.vaadin.com/directory/${this.version?.installs[key]}"><iron-icon icon="lumo:download"></iron-icon><span>Download Zip</span></a></span></div>` :
            html`<pre class="text-xs" @click="${this.copyToClipboard}" id="install-tabsheets-${key}">${this.version?.installs[key]}</pre>`
            }
            </div>
          `)}
        </div>
        `;
  }

  get selectedTab(): number | null | undefined {
    let container = this.querySelector("#tabs");
    return container &&
      container.getAttribute("selected") != undefined ?
      Number.parseInt(""+container.getAttribute("selected")) :
      undefined;
  }

  get tabs() {
    let container = this.querySelector("#tabs");
    if (container) { return Array.from(container.children) ; }
      return [];
    }

  get sheets() {
    let container = this.querySelector("#sheets");
    if (container) { return Array.from(container.children) ; }
    return [];
  }

  get selectedSheet() {
    let t = this.selectedTab;
    let s = this.sheets;
    return t != undefined && s ? s[t] : undefined;
  }

  tabChanged(e: CustomEvent | null) {
    let sel = this.selectedSheet;
    this.sheets.forEach(s => {
        if (sel == s) { s.setAttribute("style", "display: block;"); }
        else { s.setAttribute("style", "display: none;"); }
        });
  }


  copyToClipboard(e: Event) {
    const element = <HTMLElement>e.target;
    if ("clipboard" in navigator) {
      try {
        navigator.clipboard.writeText(element.innerText);
      } catch (e) {
        throw new Error("Failed to copy text from "+element);
      }
    } else {
      const selection = window.getSelection();
      if (selection) {
          const currentRange = !selection ||
            selection.rangeCount === 0 ? null : selection.getRangeAt(0);
          try {
            const range = document.createRange();
            range.selectNodeContents(element);
            selection.removeAllRanges();
            selection && selection.addRange(range);
            document.execCommand("copy");
          } catch (err) {
            throw new Error("Failed to copy text from "+element);
          } finally {
            // Restore selection
            selection.removeAllRanges();
            currentRange && selection.addRange(currentRange);
          }
      }
    }
  }
}
