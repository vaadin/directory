import { Router } from '@vaadin/router';
import { routes } from './routes';
import { appStore } from './stores/app-store';

import '@fortawesome/fontawesome-free/js/brands.js';
import '@fortawesome/fontawesome-free/js/solid.js';
import '@fortawesome/fontawesome-free/js/fontawesome.js';
import { autorun } from 'mobx';

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
