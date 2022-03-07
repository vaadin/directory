import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import 'keen-slider/keen-slider.min.css';
import KeenSlider from 'keen-slider';
import type { KeenSliderInstance } from 'keen-slider';

@customElement('highlight-carousel')
export class HighlightCarousel extends View {

  @property({ attribute: false })
  addon?: Addon;

  slider?: KeenSliderInstance;

  sliderElement?: HTMLElement;
  arElement?: HTMLElement;
  alElement?: HTMLElement;

  firstUpdated() {
    this.sliderElement = this.querySelector("#slider") as HTMLElement;
    this.alElement = this.querySelector("#arrow-left") as HTMLElement;
    this.arElement = this.querySelector("#arrow-right") as HTMLElement;
    this.classList.toggle('empty', this.addon?.mediaHighlights.length === 0);
    this.addEventListener('keydown', this.keydown);
  }

  render() {
    return html`<div id="slider" tabindex="0">
        ${this.addon?.mediaHighlights.map((m,i) => html`
          <div class="item number-slide${i}">
            <img src="${m?.url}">
            <button class="btn-open" @click="${this.open}">View larger</button>
          </div>
          <button class="btn-close" @click="${this.close}">Close</button>
        `)}
      </div>
      <div class="arrow" id="arrow-left" @click="${this.prev}"></div>
      <div class="arrow" id="arrow-right" @click="${this.next}"></div>
    `;
  }

  open(e:MouseEvent) {
    const el = e.target as HTMLButtonElement;
    const item = el.parentElement;
    const main = this;
    if (item) {

      // Select the slide that was clicked
      let slideNumber = 0;
      item.classList.forEach(c => {
        if (c.startsWith('number-slide')) {
          slideNumber = parseInt(c.substring(12));
        }
      });

      main.classList.add('open');
      main.sliderElement?.focus();
      main.setupSlider(slideNumber);
    }
  }

  keydown(e: KeyboardEvent) {
    if (e.keyCode == 27) { this.close(); }
    if (e.keyCode == 37 && this.slider) { this.slider.prev(); }
    if (e.keyCode == 39 && this.slider) { this.slider.next(); }
  }

  mouseWheel(e: MouseEvent) {
    e.preventDefault();
  }

  next() {
    if (this.slider) this.slider.next();
  }

  prev() {
    if (this.slider) this.slider.prev();
  }

  close() {
    this.removeEventListener('wheel', this.mouseWheel);
    if (this.slider) this.slider.destroy();
    this.classList.remove('open');
  }

  setupSlider(slide: number) {
      if (!this.sliderElement) return;
      this.slider = new KeenSlider(
        this.sliderElement,
        {
          selector: '.item',
          initial: slide,
          slides: {
            perView: 1,
            spacing: 0,
          },
        },
        []
      );

      this.slider.on("slideChanged", (slider) => {
        const slide = slider.track.details.rel || 0;
        const max = (slider.track.details.slides.length || 0)-1;
        if (this.alElement) this.alElement.classList.toggle('disabled', slide <= 0);
        if (this.arElement) this.arElement.classList.toggle('disabled', slide >= max);

      })
      this.addEventListener('wheel', this.mouseWheel);
    }

}
