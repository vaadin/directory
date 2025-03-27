import { Route } from '@vaadin/router';
import './views/search/search-view';
import './views/addon/addon-view';
import './views/addon/card-view';
import './views/addon/stats-view';
import './views/addon/author-stats-view';

export type ViewRoute = Route & {
  title?: string;
  children?: ViewRoute[];
};

export const routes: ViewRoute[] = [
  {
    path: '/',
    component: 'search-view',
    title: 'Search',
    action: (context, commands) => { document.body.className = 'search';}
  },
  {
    path: 'trends/:user',
    component: 'author-stats-view',
    title: 'Author Stats',
    action: (context, commands) => { document.body.className = 'stats';}
  },
  {
    path: 'addon/:addon/:version?',
    component: 'addon-view',
    title: 'Addon',
    action: (context, commands) => {document.body.className = 'addon';}
  },
  {
    path: 'component/:addon/stats',
    component: 'addon-stats-view',
    title: 'Addon Stats',
    action: (context, commands) => { document.body.className = 'addon-stats';}
  },
  {
    path: 'user-stats/:user',
    component: 'author-stats-view',
    title: 'Author Stats',
    action: (context, commands) => { document.body.className = 'author-stats';}
  },
  {
    path: 'component/:addon/:version?',
    component: 'addon-view',
    title: 'Addon',
    action: (context, commands) => { document.body.className = 'addon';}
  },

  {
    path: 'addon-card/:addon',
    component: 'card-view',
    title: 'Addon',
    action: (context, commands) => { document.body.className = 'addon-card'; 
      (document.querySelector("#haas-container") as HTMLElement).style.display ='none';
      (document.querySelector(".directory-header") as HTMLElement).style.display ='none';
    }
  },

];
