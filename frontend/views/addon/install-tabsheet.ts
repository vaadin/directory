import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';

@customElement('install-tabsheet')
export class InstallTabSheet extends View {

  @property({ attribute: false })
  version?: AddonVersion;

  render() {
    if (!this.version) {
      return html`skeletor!`;
    }
    return html`
      ${Object.keys(this.version?.installs).map((key) => html`
        ${key == "Zip" ?
          html`<p><a class="download-zip" href=" https://static.vaadin.com/directory/${this.version?.installs[key]}">Download Zip</a></p>`
        :
          html`<pre @click="${this.copyToClipboard}">${this.version?.installs[key]}</pre>`}
      `)}
    `;
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
