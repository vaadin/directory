import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'Vaadin Add-on Directory';
  appDescription = 'Find open-source widgets and components for your Vaadin application.';
  appUrl = 'https://vaadin.com/directory/';
  appIcon = 'https://vaadin.com/images/trademark/PNG/VaadinLogomark_RGB_500x500.png';
  currentViewTitle = '';
  searchViewScrollTop = 0;

  constructor() {
    makeAutoObservable(this);
  }
}
export const appStore = new AppStore();
