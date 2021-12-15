import { Route } from '@vaadin/router';
import './views/search/search-view';
import './views/addon-view';

export type ViewRoute = Route & {
  title?: string;
  children?: ViewRoute[];
};

export const routes: ViewRoute[] = [
  {
    path: '',
    component: 'search-view',
    title: 'Search',
  },
  {
    path: 'addon/:addon',
    component: 'addon-view',
    title: 'Addon',
  },
  {
    path: 'component/:addon',
    component: 'addon-view',
    title: 'Addon',
  },
];
