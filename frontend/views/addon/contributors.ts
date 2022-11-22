import { html } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import { Layout } from '../view';
import '@vaadin/avatar-group/src/vaadin-avatar-group';

@customElement('github-contributors')
export class GitHubContributors extends Layout {

  @property({ attribute: true })
  repositoryUrl?:  string;

  @state()
  contributors?:  GithubUser[] = [];

  @state()
  githubUrl?: string;

  render() {
    if (!this.contributors || this.contributors.length < 2) {
      return html``;
    }

    const contributors = this.contributors.sort((a, b) => {return b.contributions - a.contributions}).map((user) => {
      return {
        name: user.login,
        img: user.avatar_url
      }
    });

    return html`
      <vaadin-avatar-group
        .items="${contributors}"
        max-items-visible="6"
        @click="${this._onClick}"
        theme="contributors">
      </vaadin-avatar-group>
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
    this.githubUrl = `https://github.com/${repo}`;
    this.contributors = await fetch("https://api.github.com/repos/"+repo+"/contributors")
                    // the JSON body is taken from the response
                    .then(res => res.json())
                    .then(res => { return res as GithubUser[]});
  }

  _onClick(e:MouseEvent) {
    console.log(e)
    if (this.repositoryUrl) {
      window.location.href = `${this.githubUrl}/contributors`;
    }
  }
}

interface GithubUser {
  id: number;
  login: string;
  avatar_url: string;
  url: string;
  contributions: number;
}
