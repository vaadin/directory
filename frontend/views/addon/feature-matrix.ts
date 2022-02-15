import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import Matrix from 'Frontend/generated/org/vaadin/directory/endpoint/search/Matrix';

@customElement('feature-matrix')
export class FeatureMatrix extends View {

  @property({ attribute: false })
  matrix?:  Matrix;

  constructor() {
    super();
  }

  render() {
    if (!this.matrix || !this.matrix.data) {
      return html`(loading data...)`;
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
            html`<tr class="row-${row} ${title?.indexOf('(')==0?'not-supported-fw':''}"><th class="rowh">${title}</th>${this.matrix?.data ?
                (this.matrix.data[row] ? this.matrix.data[row]?.map((content:string|undefined,col:number|undefined) =>
                    html`<td class="data row-${row} col-${col} ${content ? 'on':'off'}">${content}</td>`): html``) :
                html`<td colspan="${this.matrix?.cols.length}"></td>`}</tr>`
          )}
      </table>
      `;
  }

}
