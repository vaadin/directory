import { Route } from '@vaadin/router';
import './views/search-view';
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
    path: ':addon',
    component: 'addon-view',
    title: 'Addon',
  },
];
