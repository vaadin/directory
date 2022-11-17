import { Router } from '@vaadin/router';
import { routes } from './routes';
import { appStore } from './stores/app-store';
import { getAppUrl } from 'Frontend/generated/SearchEndpoint';
import { autorun } from 'mobx';
import { registerStyles, css } from '@vaadin/vaadin-themable-mixin';
import './styles/global.css';

getAppUrl().then(url => appStore.appUrl = url);
window.addEventListener('vaadin-router-error', e => {
    window.location.href = appStore.appUrl;
});
export const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);

autorun(
  () => {
    document.title =
      (appStore.currentViewTitle ? appStore.currentViewTitle + ' | ' : '') +
      appStore.applicationName;
      // Copy title to description
      const descEl = document.querySelector('head meta[name="description"]');
      if (descEl) descEl.setAttribute('content', document.title);
  }
);

function sendPageview() {
  if (location.hostname !== 'localhost' && location.hostname !== '127.0.0.1') {
    // Let vaadin.com HaaS know that the page has changed
    window.dispatchEvent(new Event('on-location-change'));
  }
}
window.addEventListener(
  'vaadin-router-location-changed',
  sendPageview as EventListener
);


/* TODO would be nice to move these to .css files */

registerStyles('vaadin-overlay', css`
  [part="overlay"] {
    background: var(--background-color);
    border-radius: var(--roundness-lg);
    box-shadow: var(--surface-shadow-m);
    border: var(--divider-color1);
    padding: var(--space-xs);
  }
`);

registerStyles('vaadin-item', css`
  :host {
    display: flex;
    align-items: center;
    box-sizing: border-box;
    border-radius: var(--roundness-md);
    cursor: pointer;
    padding: 0.5em;
  }

  :host(:hover) {
    background-color: var(--blue-100);
  }

  :host([focused]) {
    outline: none;
    box-shadow: inset 0 0 0 2px var(--blue-400);
  }

  /* This is so cumbersome */
  :host([theme~="avatar-group-item"]) ::slotted(vaadin-avatar) {
    --vaadin-avatar-size: 24px;
    background-color: var(--gray-200);
    color: inherit;
    vertical-align: middle;
    font-size: var(--text-size-lg);
  }
`);

// Using gap is not supported, need to use padding on the items
registerStyles('vaadin-menu-bar', css`
  :host([theme~="addon-version-menu"]) [part="container"] {
    overflow: visible;
  }

  :host([theme~="addon-version-menu"]) vaadin-menu-bar-button {
    padding: 0.5em 0.75em;
    cursor: pointer;
    background-color: var(--blue-500);
    color: #fff;
    border-radius: var(--roundness-md);
    margin-inline-start: 4px;
  }

  :host([theme~="addon-version-menu"]) vaadin-menu-bar-button[focused] {
    box-shadow: 0 0 0 2px #fff, 0 0 0 4px var(--blue-400);
  }

  :host([theme~="addon-version-menu"]) vaadin-menu-bar-button:hover {
    background-color: var(--blue-600);
  }
`);
