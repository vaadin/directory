import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';
import SearchResult from 'Frontend/generated/org/vaadin/directory/endpoint/search/SearchResult';
import { getAddon } from 'Frontend/generated/SearchEndpoint';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../../views/view';
import { appStore } from 'Frontend/stores/app-store';


@customElement('card-view')
export class AddonCardView extends View implements BeforeEnterObserver{

  @property()
  private addon?: SearchResult;

  updatePageMetadata (): void {
    //TODO
  }


  render() {
    return html` <addon-card .addon=${this.addon}></addon-card>`
  }

  async onBeforeEnter(location: RouterLocation) {
    const urlIdentifier = location.params.addon as string;
    const urlVersion = location.params.version as string;
    this.addon = await getAddon(urlIdentifier);
    if (this.addon) {
      appStore.currentViewTitle = this.addon.name;
      this.updatePageMetadata();
    }
  }

}
