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
    background-color: var(--blue-500);
    color: #fff;
  }
`);
