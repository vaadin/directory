import { Route } from '@vaadin/router';
import './views/search-view';
import './views/addon-view';
import './views/main-layout';

export type ViewRoute = Route & {
  title?: string;
  icon?: string;
  children?: ViewRoute[];
};

export const views: ViewRoute[] = [
  // place routes below (more info https://vaadin.com/docs/latest/fusion/routing/overview)
  {
    path: '',
    component: 'main-layout',
    icon: '',
    title: '',
  },
  {
    path: 'search',
    component: 'search-view',
    icon: 'la la-globe',
    title: 'Search',
  },
  {
    path: 'addon',
    component: 'addon-view',
    icon: 'la la-globe',
    title: 'Addon',
  },
];
export const routes: ViewRoute[] = [
  {
    path: '',
    component: 'main-layout',
    children: [...views],
  },
];
