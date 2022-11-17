import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'Vaadin Directory';
  appUrl = 'https://vaadin.com/directory/';
  currentViewTitle = '';
  searchViewScrollTop = 0;

  constructor() {
    makeAutoObservable(this);
  }
}
export const appStore = new AppStore();
