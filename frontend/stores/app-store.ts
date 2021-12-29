import { RouterLocation } from '@vaadin/router';
import { makeAutoObservable } from 'mobx';

export class AppStore {
  applicationName = 'Vaadin Directory';
  currentViewTitle = '';

  constructor() {
    makeAutoObservable(this);
  }
}
export const appStore = new AppStore();
