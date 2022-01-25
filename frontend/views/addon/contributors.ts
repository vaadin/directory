import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import '@vaadin/avatar-group/src/vaadin-avatar-group';

@customElement('github-contributors')
export class GitHubContributors extends View {

  @property({ attribute: true })
  repositoryUrl?:  String;

  @property({ attribute: false })
  contributors?:  GithubUser[] = [];

  constructor() {
    super();
  }

  render() {
    if (!this.contributors || this.contributors.length < 2) {
      return html``;
    }

    const contributors = this.contributors.sort((a, b) => {return a.contributions - b.contributions}).map((user) => {
      return {
        name: user.login,
        img: user.avatar_url
      }
    });

    return html`
      <a href="${this.repositoryUrl}/contributors" class="contributors">${this.contributors.length} contributors</a>
      <vaadin-avatar-group .items="${contributors}" max-items-visible="5"></vaadin-avatar-group>
    `;
  }

  firstUpdated() {
    this.fetchContributors();
  }

  async fetchContributors(){
    if (!this.repositoryUrl) { return; }
    const matches = this.repositoryUrl.match(/http(?:s)?:\/\/github.com\/([-_\w\d]+\/[-_\w\d]+)(?:.git)?/);
    const repo = matches && matches.length > 1? matches[1]: undefined;
    if (!repo) { return; }
    this.contributors = await fetch("https://api.github.com/repos/"+repo+"/contributors")
                    // the JSON body is taken from the response
                    .then(res => res.json())
                    .then(res => { return res as GithubUser[]});
  }
}

interface GithubUser {
  id: number;
  login: string;
  avatar_url: string;
  url: string;
  contributions: number;
}
