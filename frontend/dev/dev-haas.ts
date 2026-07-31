// Local-dev only. index.ts imports this behind a `localhost` hostname check, so it
// is a lazy chunk that is only ever fetched on localhost (never by production users).
//
// There is no real HaaS/SSO on localhost, so we stub `window.haas` and back it
// with the server-side dev faker (POST /authfaker, gated by
// APP_AUTHENTICATION_ENABLE_AUTH_FAKER). On localhost index.html skips loading the
// real haas-loader, so this stub is the only window.haas. Clicking the dev "Log in"
// button (window.haas.login()) authenticates BOTH the client UI and the server
// session, exercising the real server-side identity path.
//
// `window.haas`'s global type lives in views/view.ts (interface Haas). It only
// declares the members the app reads (isAuthenticated/userInfo); this dev stub
// additionally provides loader/login/logout, so it is built as a local DevHaas
// and installed with a cast.

import type { Haas } from '../views/view';

interface DevHaas extends Haas {
  loader: { initMenu: () => void };
  login: () => Promise<void>;
  logout: () => Promise<void>;
}

const SCREENNAME_KEY = 'dev-haas-screenname';

export function installDevHaas(): void {
  // Stand-in for the real HaaS header menu, which is what renders the login control into
  // #haas-container in production. Without it there is no visible menu (and no way to log in)
  // on the index page in local dev.
  const haasContainer = document.querySelector('#haas-container');
  const renderDevMenu = () => {
    if (!haasContainer) return;
    const authed = !!stub.isAuthenticated;
    const name = stub.userInfo && stub.userInfo.screenname;
    haasContainer.textContent = '';
    const bar = document.createElement('div');
    bar.className = 'dev-haas-menu';
    bar.style.cssText =
      'display:flex;gap:.5rem;align-items:center;justify-content:flex-end;padding:.25rem .75rem;font-size:.875rem;';
    const label = document.createElement('span');
    label.textContent = authed ? 'Logged in as ' + name + ' (dev)' : 'Not logged in (dev)';
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = authed ? 'Log out' : 'Log in';
    button.onclick = () => (authed ? stub.logout() : stub.login());
    bar.append(label, button);
    haasContainer.appendChild(bar);
  };

  const applyUser = (screenname: string | null) => {
    stub.isAuthenticated = !!screenname;
    stub.userInfo = (screenname
      ? { authenticated: true, screenname, firstname: screenname, lastname: '', portraitUrl: '' }
      : {}) as Haas['userInfo'];
    renderDevMenu();
    window.dispatchEvent(new Event('haas-user-info-changed'));
  };

  const authfaker = async (screenname: string | null): Promise<boolean> => {
    const url = screenname
      ? '/authfaker?screenname=' + encodeURIComponent(screenname)
      : '/authfaker';
    const res = await fetch(url, { method: 'POST' });
    if (res.status === 404) {
      console.warn('[dev-haas] POST /authfaker returned 404 — the faker is only active in '
        + 'Vaadin dev mode. Run the app with `mvn spring-boot:run` (not a production build) '
        + 'for local login.');
      return false;
    }
    return res.ok;
  };

  const stub: DevHaas = {
    isAuthenticated: false,
    userInfo: {} as Haas['userInfo'],
    loader: { initMenu: () => renderDevMenu() },
    login: async () => {
      const screenname = window.prompt('Dev login — screen name:',
        localStorage.getItem(SCREENNAME_KEY) || '');
      if (!screenname) return;
      if (await authfaker(screenname)) {
        localStorage.setItem(SCREENNAME_KEY, screenname);
        applyUser(screenname);
      }
    },
    logout: async () => {
      await authfaker(null);
      localStorage.removeItem(SCREENNAME_KEY);
      applyUser(null);
    },
  };

  // On localhost the real haas-loader is never loaded (index.html skips it), so a plain
  // assignment is enough — nothing competes to overwrite window.haas.
  window.haas = stub;

  // Show the anonymous dev menu immediately, then re-establish a remembered session
  // (a fresh JSESSIONID needs the faker POST again — applyUser re-renders on success).
  renderDevMenu();
  const remembered = localStorage.getItem(SCREENNAME_KEY);
  if (remembered) {
    authfaker(remembered).then((ok) => { if (ok) applyUser(remembered); });
  }
}
