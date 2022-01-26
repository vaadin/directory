import { Router } from '@vaadin/router';
import { routes } from './routes';
import { appStore } from './stores/app-store';
import { autorun } from 'mobx';
import { registerStyles, css } from '@vaadin/vaadin-themable-mixin';
import './styles/global.css';

export const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);

autorun(
  () =>
    (document.title =
      (appStore.currentViewTitle ? appStore.currentViewTitle + ' | ' : '') +
      appStore.applicationName)
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
  :host([theme~="addon-version-menu"]) vaadin-menu-bar-button {
    padding: 0.25em 0.5em;
    color: var(--blue-500);
    cursor: pointer;
    border: 1px solid var(--blue-200);
    border-radius: var(--roundness-md);
    margin-inline-start: 4px;
  }

  :host([theme~="addon-version-menu"]) vaadin-menu-bar-button:hover {
    color: var(--blue-600);
  }
`);
