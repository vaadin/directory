import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { logAddonInstall, getAddonInstalls } from 'Frontend/generated/AddonEndpoint';
import { html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { Layout } from '../view';
import '@vaadin/menu-bar/src/vaadin-menu-bar';
import {MenuBarItem}  from '@vaadin/menu-bar/src/vaadin-menu-bar';
import { router } from '../../index';

@customElement('install-tabsheet')
export class InstallTabSheet extends Layout {

  @property({ attribute: false })
  addon?: Addon;

  @property({ attribute: false })
  version?: AddonVersion;


  @property({ attribute: false })
  user?: String;

  @state()
  private installs: String[] = [];

  render() {
    if (!this.version || !this.addon) {
      return html`No version selected`;
    }

    const download = document.createElement('a');
    download.href = `${this.version.installs['Zip']}`;
    download.onclick = () => { logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "zip", this.getCurrentUserId()); }

    download.textContent = 'Download ZIP';

    const create = document.createElement('a');
    create.href = `${this.addon.addonProjectDownloadBaseUrl}?addons=${this.addon.urlIdentifier}/${this.version?.name}`;
    create.onclick = () => { logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "create", this.getCurrentUserId()); }
    create.innerHTML = '<div>Create project</div><span>Create and download a new project using this add-on</span>';

    const copyMaven = document.createElement('button');
    copyMaven.onclick = () => {
      logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "maven", this.getCurrentUserId());
      this.copyToClipboard(this.version?.installs['Maven']);

      if (copyMaven.getElementsByTagName('pre').length == 0) {
        const  snippet = document.createElement('pre');
        snippet.innerText = this.version?.installs['Maven'] || '';
        copyMaven.appendChild(snippet);
      }
      copyMaven.firstElementChild!.textContent = 'Copied ✔';
      setTimeout(() => {
        copyMaven.firstElementChild!.textContent = 'Maven POM';
        this.updateInstallInfo();
      }, 10000);
    };
    copyMaven.innerHTML = '<div>Maven POM</div><span>View and copy Maven dependency XML to clipboard</span>';

    const mavenText = document.createElement('pre');
    mavenText.innerText = this.version.installs['Maven'] || '';

    const linkToVersion = document.createElement('a');
    linkToVersion.href = router.urlForPath('component/:addon/:version?', { addon: this.addon.urlIdentifier, version: this.version?.name });
    linkToVersion.innerText = 'Link to this version';

    const compatibilityIssues = document.createElement('a');
    compatibilityIssues.href = location.href + '#discussion';
    compatibilityIssues.innerText = 'Report compatibility issues';

    const previousInstalls = document.createElement('div');
    previousInstalls.appendChild(document.createElement('hr'));
    const previousInstallsTitle = document.createElement('i');
    previousInstallsTitle.innerText = 'Previously installed:';
    previousInstalls.appendChild(previousInstallsTitle);
    if (this.installs.length > 0) {
      this.installs.forEach((s) => {
        const linkToVersion = document.createElement('a');
        linkToVersion.href = router.urlForPath('component/:addon/:version?', { addon: ''+this.addon?.urlIdentifier, version: s.split('/')[0] });
        linkToVersion.innerText = s.split('/')[0]+' ('+s.split('/')[1]+')';
        linkToVersion.title = 'Installed at '+s.split('/')[2]+'';
        previousInstalls.appendChild(linkToVersion);
      });
    } else {
        const na = document.createElement('div');
        na.innerText = '(no previous installs)';
        previousInstalls.appendChild(na);
    }

    const menuItems = Object.keys(this.version.installs);
    if (menuItems.indexOf('Maven') >=0) menuItems.push("Create");
    menuItems.push("Previous");
    const options: MenuBarItem[] = [
      {
        text: 'Install',
        children: menuItems.map((key) => {
          if (key == 'Zip') {
            return { component: download };
          } else if (key == 'Maven') {
            return { component: ("clipboard" in navigator) ? copyMaven : mavenText};
          } else if (key == 'Create') {
            return { component: create };
          } else if (key == 'Previous') {
            return { component: previousInstalls };
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

    if (!this.getCurrentUserId()) {
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

  firstUpdated() {
    setTimeout(() => {this.updateInstallInfo();},0);
  }

  copyToClipboard(content: string|undefined) {
      try {
        navigator.clipboard.writeText(content || '');
      } catch (e) {
        throw new Error("Failed to copy content to clipboard");
      }
  }

  async updateInstallInfo() {
    getAddonInstalls(this.addon?.urlIdentifier, this.getCurrentUserId())
      .then(installs => {
        this.installs = installs;
    });
  }
}
