import '@vaadin/text-field';
import '@vaadin/grid';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset';
import '@vaadin/button';
import './addon-card';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import { FilterAddedEvent } from './filter-added-event';
import { searchStore } from './search-store';
import { appStore } from 'Frontend/stores/app-store';
import { AppEndpoint } from 'Frontend/generated/endpoints';
import VersionInfo from 'Frontend/generated/org/vaadin/directory/endpoint/app/VersionInfo';
import 'keen-slider/keen-slider.min.css'
import KeenSlider from 'keen-slider';
import type { KeenSliderInstance } from 'keen-slider';
import { observe } from "mobx"
import { disqusReset } from "../disqus"

@customElement('search-view')
export class SearchView extends View {

  @property()
  private versionInfo: string = "(build info)"

  featuredSlider?: KeenSliderInstance;

  constructor() {
    super();
    appStore.currentViewTitle = 'Search';
  }
  render() {
    return html`
      <div class="flex flex-col items-center">
        <span id="build-info"><a target="dgithub" href="https://github.com/vaadin/directory/commits/main">${this.versionInfo}</a></span>
        <h1>Add-ons, cool widgets, and integrations for Vaadin</h1>
      </div>
      <div id="featured-list" class="keen-slider">
        ${searchStore.featured.map(
          (addon,i) => html` <div class="keen-slider__slide number-slide${i}"><addon-card .addon=${addon}></addon-card></div> `
        )}
        ${this.setupFeaturedSliderIfNeeded()}
      </div>
      <p>
        Want to publish your work here? Great! <a href="javascript:window.haas.login()">Log in</a> and <a href="https://vaadin.com/directory/help">follow the instructions</a>.
      </p>
      <h2>Search for add-ons</h2>
      <vaadin-text-field
        style="min-width: 400px; max-width: 640px;"
        placeholder="Try e.g. 'upload' or 'icons'"
        .value=${searchStore.query}
        @change=${this.updateQuery}
        clear-button-visible>
      </vaadin-text-field>
      <div>Found total <b>${searchStore.totalCount}</b> add-ons. <i class="text-2xs">Want to narrow down? Try filters like <a href="?q=v%3A8">v:8</a> or <a href="?q=author%3Ame">author:me</a></i></div>
      <div class="addons-grid" @filter-added=${this.filterAdded}>
        ${searchStore.addons.map(
          (addon) => html` <addon-card .addon=${addon}></addon-card> `
        )}
      </div>

      <vaadin-button
        id="load-more-button"
        @click=${searchStore.fetchPage}
        ?disabled=${searchStore.loading}
        ?hidden=${searchStore.addons.length === 0}>
        Load more
      </vaadin-button>
    `;
  }

  firstUpdated() {
    searchStore.init();
    this.setupIntersectionObserver();

    AppEndpoint.getVersionInfo().then(v => {
      this.versionInfo = v.version +" / " + v.buildTime + " / " + v.startTime;
    });

    // Reset discuss thread
    disqusReset("search", "https://directory4.demo.vaadin.com", "Vaadin Directory Search", false);
    this.setupFeaturedSliderIfNeeded();
    this.restoreScrollIfNeeded();
  }

  restoreScrollIfNeeded() {
    if (window.searchScroll && window.searchScroll > 0) {
        // TODO: Really, this trick again...
        setTimeout(function () { window.scroll(0, window.searchScroll);  }, 500);
    }
  }

  setupFeaturedSliderIfNeeded() {
    const elem: HTMLElement = <HTMLElement>this.renderRoot.querySelector("#featured-list");
    if (!elem
        || elem.childElementCount <= 0
        || (this.featuredSlider && elem.childElementCount === this.featuredSlider.slides.length)) return;
    this.featuredSlider = new KeenSlider(elem, {
      initial: 0,
      loop: true,
      slides: {
        perView: 1,
        spacing: 10,
      }
    },
    [
      (slider) => {
        let timeout : any;
        let mouseOver = false
        function clearNextTimeout() {
          clearTimeout(timeout)
        }
        function nextTimeout() {
          clearTimeout(timeout)
          if (mouseOver) return
          timeout = setTimeout(() => {
            slider.next()
          }, 4000)
        }
        slider.on("created", () => {
          slider.container.addEventListener("mouseover", () => {
            mouseOver = true
            clearNextTimeout()
          })
          slider.container.addEventListener("mouseout", () => {
            mouseOver = false
            nextTimeout()
          })
          nextTimeout()
        })
        slider.on("dragStarted", clearNextTimeout)
        slider.on("animationEnded", nextTimeout)
        slider.on("updated", nextTimeout)
      },
    ]
    );
  }

  setupIntersectionObserver() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) searchStore.fetchPage();
      });
    });
    const button = this.renderRoot.querySelector('#load-more-button');
    if (button) {
      observer.observe(button);
    }
  }

  filterAdded({ filter }: FilterAddedEvent) {
    searchStore.addFilter(filter);
  }

  updateQuery(e: { target: HTMLInputElement }) {
    searchStore.setQuery(e.target.value);
  }
}
