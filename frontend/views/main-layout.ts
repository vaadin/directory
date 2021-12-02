import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { router } from '../index';
import { views } from '../routes';
import { appStore } from '../stores/app-store';
import { Layout } from './view';

@customElement('main-layout')
export class MainLayout extends Layout {
  render() {
    return html`
        <!-- TODO: We want to get rid of Shadow DOM early -->
        <div class="main"><slot></slot></div>
    `;
  }

}
