import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { Layout } from '../view';
import {appStore} from '../../stores/app-store'
import { SearchEndpoint } from 'Frontend/generated/endpoints';
import Matrix from 'Frontend/generated/org/vaadin/directory/endpoint/search/Matrix';

@customElement('feature-matrix')
export class FeatureMatrix extends Layout {

  @property({ attribute: false })
  addon?:  string;

  @property({ attribute: false })
  matrix?:  Matrix;

  intersecting: boolean = false;

  observer?: IntersectionObserver;

  constructor() {
    super();
  }

  connectedCallback() {
    super.connectedCallback();
    this.initIntersectionObserver();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.disconnectObserver();
  }

  render() {
    if (!this.matrix || !this.matrix.data) {
      return html`(Loading compatibility data...)`;
    }
    return html`
      <table class="matrix">
        <tr>
          <th class="rowh"></th>
          ${this.matrix.cols?.map((title,col) =>
            html`<th class="colh col-${col}">${title}</th>`
          )}
        </tr>
          ${this.matrix.rows?.map((title,row) =>
            html`<tr class="row row-${row} ${title?.indexOf('-')==0?'collapsed':''} ${title?.indexOf('(')==0?'not-supported-fw':''}"><th class="rowh">${title}</th>${this.matrix?.data ?
                (this.matrix.data[row] ? this.matrix.data[row]?.map((content:string|undefined,col:number|undefined) =>
                    html`<td class="data row-${row} col-${col} ${content ? 'on':'off'}"><a style="color:inherit; text-decoration: inherit;" href="${content? appStore.appUrl +'component/'+ this.addon! +'/'+ this.matrix!.cols[col!] :''}">${content}</a></td>`): html``) :
                html`<td colspan="${this.matrix?.cols.length}"></td>`}</tr>`
          )}
      </table>
      `;
  }

  preProcess(input: Matrix) {
    //input.forEach
    let combined = JSON.stringify(input.data[0]);
    let combinedi = 0;
    input.data.forEach((d,i) => {
      if (i > 0) {
        const supported =  !((''+input.rows[i]).indexOf('(') === 0);
        if (JSON.stringify(d) === combined && supported) {
          input.rows[i] = '-'+input.rows[i];
        } else {
          // no match, close te combined if open
          if (combinedi < i-1) {
            input.rows[combinedi] = (''+input.rows[i-1]).substring(1) + '-' +input.rows[combinedi];
          }
          combined = JSON.stringify(d);
          combinedi = i;
        }
      }


    });
    return input;
  }

  initIntersectionObserver() {

    // Fail-safe
    if (!('IntersectionObserver' in window))  {
      this.intersecting = true;
      return;
    }

    // Init once
    if (this.observer) return;
    this.observer = new IntersectionObserver((entries: IntersectionObserverEntry[]) => {
      entries.forEach((entry) => {
        // Toggle state
        if (entry.isIntersecting != this.intersecting) this.intersecting = entry.isIntersecting;
        if (this.intersecting && !this.matrix) {
          SearchEndpoint.getCompatibility(this.addon).then(value => this.matrix = this.preProcess(value));
        }
      });
    }, { rootMargin: "50px" }); // 50px margin

    this.observer.observe(this);
  }

  disconnectObserver() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

}
