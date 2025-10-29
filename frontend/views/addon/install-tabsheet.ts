import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import AddonVersion from 'Frontend/generated/org/vaadin/directory/endpoint/addon/AddonVersion';
import { logAddonInstall, getAddonInstalls } from 'Frontend/generated/AddonEndpoint';
import { html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { Layout } from '../view';
import '@vaadin/menu-bar';
import type { MenuBarItem } from '@vaadin/menu-bar';
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

    const options = this.createInstallMenuOptions();
    const extraOptions = this.createExtraMenuOptions();

    return html`
      <vaadin-menu-bar .items="${options}" theme="addon-version-menu"></vaadin-menu-bar>
      <vaadin-menu-bar .items="${extraOptions}" theme="addon-version-menu"></vaadin-menu-bar>
    `;
  }

  private createInstallMenuOptions(): MenuBarItem[] {
    const installButton = document.createElement('div');
    installButton.textContent = 'Install...';

    // Add hover listener to check authentication status
    installButton.addEventListener('mouseenter', () => {
      this.checkAuthenticationAndUpdate();
    });

    if (!this.isAuthenticated()) {
      return [{
        component: installButton,
        children: [{
          text: 'Log in to install',
          component: this.createLoginElement()
        }]
      }];
    }

    const menuItems = [...Object.keys(this.version!.installs)];
    if (menuItems.includes('Maven')) {
      menuItems.push("Create");
    }
    menuItems.push("Previous");

    return [{
      component: installButton,
      children: menuItems.map(key => this.createMenuItemForType(key))
    }];
  }

  private checkAuthenticationAndUpdate(): void {
    // Force a re-render to update the menu items based on current auth state
    console.log("Checking authentication status for install menu...");
    if (this.isAuthenticated()) {
      this.user = this.getCurrentUserId();
    }
  }

  private createMenuItemForType(key: string): MenuBarItem {
    switch (key) {
      case 'Zip':
        return { component: this.createDownloadZipElement() };
      case 'Maven':
        return { component: this.supportsCopyToClipboard() ? this.createCopyMavenElement() : this.createMavenTextElement() };
      case 'Create':
        return { component: this.createProjectElement() };
      case 'Previous':
        return { component: this.createPreviousInstallsElement() };
      default:
        return {};
    }
  }

  private createExtraMenuOptions(): MenuBarItem[] {
    return [{
      text: '▼',
      children: [
        { component: this.createLinkToVersionElement() },
        { component: this.createCompatibilityIssuesElement() }
      ]
    }];
  }

  private createDownloadZipElement(): HTMLAnchorElement {
    const download = document.createElement('a');
    download.href = this.version!.installs['Zip'];
    download.textContent = 'Download ZIP';
    download.onclick = () => {
      logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "zip", this.getCurrentUserId());
    };
    return download;
  }

  private createProjectElement(): HTMLAnchorElement {
    const create = document.createElement('a');
    create.href = `${this.addon!.addonProjectDownloadBaseUrl}?addons=${this.addon!.urlIdentifier}/${this.version?.name}`;
    create.innerHTML = '<div>Create project</div><span>Create and download a new project using this add-on</span>';
    create.onclick = () => {
      logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "create", this.getCurrentUserId());
    };
    return create;
  }

  private createCopyMavenElement(): HTMLParagraphElement {
    const copyMaven = document.createElement('p');
    copyMaven.innerHTML = '<div>Maven POM</div><span>View and copy Maven dependency XML to clipboard</span>';
    copyMaven.onclick = () => this.handleMavenCopy(copyMaven);
    return copyMaven;
  }

  private handleMavenCopy(copyMaven: HTMLParagraphElement): void {
    logAddonInstall(this.addon?.urlIdentifier, this.version?.name, "maven", this.getCurrentUserId());

    const text = this.version?.installs['Maven'] || '';
    const [textDep, textRepo] = text.split('\n<!-- Vaadin Maven repository -->\n') || '';
    this.copyToClipboard(textDep);

    if (copyMaven.getElementsByTagName('pre').length === 0) {
      this.addMavenSnippets(copyMaven, textDep, textRepo);
    }

    this.showCopySuccessMessage(copyMaven);
  }

  private addMavenSnippets(container: HTMLParagraphElement, textDep: string, textRepo?: string): void {
    const snippet = document.createElement('pre');
    snippet.textContent = textDep;
    container.appendChild(snippet);

    if (textRepo) {
      const repoInfo = document.createElement('span');
      const repoSnippet = document.createElement('pre');
      repoInfo.textContent = "Make sure you also have Vaadin Maven repository:";
      repoSnippet.textContent = textRepo;
      container.appendChild(repoInfo);
      container.appendChild(repoSnippet);
    }
  }

  private showCopySuccessMessage(copyMaven: HTMLParagraphElement): void {
    copyMaven.firstElementChild!.textContent = 'Copied dependency to clipboard ✔';
    setTimeout(() => {
      copyMaven.firstElementChild!.textContent = 'Maven POM';
      this.updateInstallInfo();
    }, 10000);
  }

  private createMavenTextElement(): HTMLPreElement {
    const mavenText = document.createElement('pre');
    mavenText.innerText = this.version!.installs['Maven'] || '';
    return mavenText;
  }

  private createLoginElement(): HTMLDivElement {
    const login = document.createElement('div');
    login.innerText = 'Log in to install!';
    login.onclick = () => window.haas.login();
    return login;
  }

  private createLinkToVersionElement(): HTMLAnchorElement {
    const linkToVersion = document.createElement('a');
    linkToVersion.href = router.urlForPath('component/:addon/:version?', {
      addon: this.addon!.urlIdentifier,
      version: this.version?.name
    });
    linkToVersion.innerText = 'Link to this version';
    return linkToVersion;
  }

  private createCompatibilityIssuesElement(): HTMLAnchorElement {
    const compatibilityIssues = document.createElement('a');
    compatibilityIssues.href = location.href + '#discussion';
    compatibilityIssues.innerText = 'Report compatibility issues';
    return compatibilityIssues;
  }

  private createPreviousInstallsElement(): HTMLDivElement {
    const previousInstalls = document.createElement('div');
    previousInstalls.appendChild(document.createElement('hr'));

    const title = document.createElement('i');
    title.innerText = 'Previously installed:';
    previousInstalls.appendChild(title);

    if (this.installs.length > 0) {
      this.addPreviousInstallLinks(previousInstalls);
    } else {
      this.addNoInstallsMessage(previousInstalls);
    }

    return previousInstalls;
  }

  private addPreviousInstallLinks(container: HTMLDivElement): void {
    this.installs.forEach((installInfo) => {
      const [version, method, timestamp] = installInfo.split('/');
      const link = document.createElement('a');
      link.href = router.urlForPath('component/:addon/:version?', {
        addon: this.addon!.urlIdentifier,
        version
      });
      link.innerText = `${version} (${method})`;
      link.title = `Installed at ${timestamp}`;
      container.appendChild(link);
    });
  }

  private addNoInstallsMessage(container: HTMLDivElement): void {
    const message = document.createElement('div');
    message.innerText = this.isAuthenticated()
      ? '(no previous installs)'
      : '(log in to see previous installs)';
    container.appendChild(message);
  }

  private supportsCopyToClipboard(): boolean {
    return 'clipboard' in navigator;
  }

  connectedCallback() {
    super.connectedCallback();
    // Listen anywhere in the document (or use this.addEventListener for host-only)
    window.addEventListener('haas-user-info-changed', () => {
      console.log("Updating install menu...");
      this.requestUpdate();
    }, false);
  }

  disconnectedCallback() {
    window.removeEventListener('haas-user-info-changed', this.onLogin as EventListener);
    super.disconnectedCallback();
  }

  private onLogin = () => {
    // Force re-render when user info changes
    this.requestUpdate();
  };

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
    if (!this.isAuthenticated()) {
        this.installs = [];
        return;
    }
    getAddonInstalls(this.addon?.urlIdentifier, this.getCurrentUserId())
      .then((installs: String[]) => {
        this.installs = installs;
    });
  }
}
