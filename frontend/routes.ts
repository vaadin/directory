import { Route } from '@vaadin/router';
import './views/search/search-view';
import './views/addon/addon-view';

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
    path: 'addon/:addon/:version?',
    component: 'addon-view',
    title: 'Addon',
    action: (context, commands) => {document.body.className = 'addon';}
  },
  {
    path: 'component/:addon/:version?',
    component: 'addon-view',
    title: 'Addon',
    action: (context, commands) => { document.body.className = 'addon';}
  },
];
