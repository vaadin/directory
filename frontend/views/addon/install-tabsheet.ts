import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { View } from '../view';
import '@vaadin/menu-bar/src/vaadin-menu-bar';
import {MenuBarItem}  from '@vaadin/menu-bar/src/vaadin-menu-bar';
import { router } from '../../index';

@customElement('install-tabsheet')
export class InstallTabSheet extends View {

  @property({ attribute: false })
  addon?: Addon;

  @property({ attribute: false })
  version?: AddonVersion;

  // TODO: User information missing
  @state()
  private user?: Object = {};

  render() {
    if (!this.version || !this.addon) {
      return html`skeletor!`;
    }

    const download = document.createElement('a');
    download.href = `https://static.vaadin.com/directory/${this.version.installs['Zip']}`;
    download.textContent = 'Download ZIP';

    const copyMaven = document.createElement('button');
    copyMaven.onclick = () => { this.copyToClipboard(this.version?.installs['Maven']) };
    copyMaven.innerHTML = 'Maven <span>Copy Maven dependency XML to clipboard</span>';

    const mavenText = document.createElement('pre');
    mavenText.innerText = this.version.installs['Maven'] || '';

    const linkToVersion = document.createElement('a');
    linkToVersion.href = router.urlForPath('addon/:addon/:version?', { addon: this.addon.urlIdentifier, version: this.version?.name });
    linkToVersion.innerText = 'Link to this version';

    const compatibilityIssues = document.createElement('a');
    compatibilityIssues.href = location.href + '#discussion';
    compatibilityIssues.innerText = 'Report compatibility issues';

    const options: MenuBarItem[] = [
      {
        text: 'Install',
        children: Object.keys(this.version.installs).map((key) => {
          if (key == 'Zip') {
            return { component: download };
          } else if (key == 'Maven') {
            return { component: ("clipboard" in navigator) ? copyMaven : mavenText};
          } else {
            return {}
          }
        })
      }];
    const extraOptions: MenuBarItem[] = [
      {
        text: '···',
        children: [
          {
            component: linkToVersion
          },
          {
            component: compatibilityIssues
          }
        ]
      }
    ];

    if (!this.user) {
      options[0].children = [
        {
          text: 'Log in to install',
          component: undefined
        }
      ]
    }

    return html`
      <vaadin-menu-bar .items="${options}" theme="addon-version-menu"></vaadin-menu-bar>
      <vaadin-menu-bar .items="${extraOptions}" theme="addon-version-menu"></vaadin-menu-bar>
    `;
  }

  copyToClipboard(content: string|undefined) {
      try {
        navigator.clipboard.writeText(content || '');
      } catch (e) {
        throw new Error("Failed to copy content to clipboard");
      }
  }
}
