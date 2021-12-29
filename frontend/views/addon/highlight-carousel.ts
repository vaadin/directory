import Addon from 'Frontend/generated/org/vaadin/directory/endpoint/addon/Addon';
import { html } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import { View } from '../view';
import 'keen-slider/keen-slider.min.css'
import KeenSlider from 'keen-slider';
import type { KeenSliderInstance } from 'keen-slider';

@customElement('highlight-carousel')
export class HighlightCarousel extends View {

  @property({ attribute: false })
  addon?: Addon;

  @property({ attribute: false })

  index: number = 0;

  slider?: KeenSliderInstance;

  numberOfSlides: number = 0;

  constructor() {
    super();
  }

  render() {
    if (!this.addon) {
      return html`skeletor!`;
    }
    return html`
      <div class="highlight-carousel">
        <div id="slides" class="keen-slider">
          ${this.addon.mediaHighlights.map((m,i) =>
            html`<div class="keen-slider__slide number-slide${i}"><img src="${m?.url}"></img></div>`
          )}
        </div>
      </div>
      `;
  }

  next() {
    this.slider && this.slider.next();
  }

  prev() {
    this.slider && this.slider.prev();
  }

  buttonClick(e: Event) {
    if (!e || !e.target) { return; }
    const elem = <HTMLElement>e.target;
    this.selectSlide(parseInt(""+elem.getAttribute("key")));
  }

  selectSlide(i: number) {
    if (this.slider) {
      this.slider.moveToIdx(i);
    }
  }

  firstUpdated() {
    if (!this.addon) return;
    this.numberOfSlides = this.addon.mediaHighlights.length < 4 ? this.addon.mediaHighlights.length : 3;
    const elem: HTMLElement = <HTMLElement>this.querySelector("#slides");
    this.slider = new KeenSlider(elem, {
      initial: 0,
      loop: false,
      slides: {
        perView: this.numberOfSlides ,
        spacing: 10,
      },
      slideChanged: () => {
        if (!this.slider) return;
        const next = this.slider.track.details.abs || 0;
        this.index = this.slider.track.absToRel(next);
      },
    });

    //TODO: we could show a larger version here
    this.slider.slides.forEach((slide, idx) => {
      slide.addEventListener("click", () => {
        this.removeActive();
        this.addActive(idx);
        console.log(idx+ "  "+this.index);
      })
    })
  }

  private removeActive() {
    if (!this.addon || !this.slider) return;
    this.slider.slides.forEach((slide) => {
      slide.classList.remove("active")
    })
  }

  private addActive(idx: number) {
    if (!this.addon || !this.slider) return;
    this.slider.slides[idx].classList.add("active")
  }

}
