import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import {guard} from 'lit/directives/guard.js';
import { View } from '../view';

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
    return html`
      <a href="${this.repositoryUrl}/contributors" class="contributors">${guard([this.contributors], () => this.contributors && this.contributors.length > 0 ?
        html`${this.contributors.sort((a, b) => {return a.contributions - b.contributions}).map((user) =>
          html`<img class="avatar" src="${user.avatar_url}" title="${user.login}"/>`)}` :
          html``)}</a> <span class="total">${this.contributors.length} contributors</span>
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