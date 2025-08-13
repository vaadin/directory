import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'Vaadin Add-on Directory';
  appDescription = 'Find open-source widgets, add-ons, themes, and integrations for your Vaadin application.';
  appUrl = 'https://vaadin.com/directory/';
  appIcon = 'https://website.vaadin.com/hubfs/Logos/vaadin_symbol_RGB_500x500.png';
  currentViewTitle = this.applicationName;
  currentViewDescription = this.appDescription;
  searchViewScrollTop = 0;

  constructor() {
    makeAutoObservable(this);
  }

  setCurrentViewTitle(title: string) {
      this.currentViewTitle = title;
  }

}
export const appStore = new AppStore();
