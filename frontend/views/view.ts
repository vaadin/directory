import { MobxLitElement } from '@adobe/lit-mobx';
import { autorun, IAutorunOptions, IReactionDisposer, IReactionOptions, IReactionPublic, reaction } from 'mobx';
import { BeforeEnterObserver, RouterLocation } from '@vaadin/router';

export interface HaasUserInfo {
  authenticated: boolean;
  screenname: string;
  firstname: string;
  lastname: string;
  portraitUrl: string;
}

export interface Haas {
  isAuthenticated: boolean;
  userInfo: HaasUserInfo;
}

declare global { interface Window { searchScroll : number; } }
declare global { interface Window { haas : Haas; } }

const USER_NOT_LOGGED_IN = "(not logged in)";

export class MobxElement extends MobxLitElement {
  private disposers: IReactionDisposer[] = [];

  /**
   * Find the current user id. Either logged in or generated.
   */
   protected getCurrentUserId(): string {

      // Use the userid provided by authenticated
      if (this.isAuthenticated() && window.haas.userInfo) {
          return window.haas.userInfo.screenname;
      }

      return USER_NOT_LOGGED_IN;
   }


  /**
   * Check if user has been authenticated.
   */
   protected isAuthenticated(): boolean {
      return (window.haas
        && window.haas.isAuthenticated);
  }

  /**
   * Creates a MobX reaction using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected reaction<T, FireImmediately extends boolean = false>(
    expression: (r: IReactionPublic) => T,
    effect: (arg: T, prev: FireImmediately extends true ? T | undefined : T, r: IReactionPublic) => void,
    opts?: IReactionOptions<T, FireImmediately>
  ): void {
    this.disposers.push(reaction(expression, effect, opts));
  }

  /**
   * Creates a MobX autorun using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected autorun(view: (r: IReactionPublic) => any, opts?: IAutorunOptions): void {
    this.disposers.push(autorun(view, opts));
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
    this.disposers.forEach((disposer) => {
      disposer();
    });
    this.disposers = [];
  }
}

/**
 * A view is a container that holds all UI elements, layouts and styling of a section of the application. A view is
 * usually mapped under a certain URL.
 *
 * By default, views don't use shadow root to render their children, which means that any elements added directly to a
 * view are rendered into the light DOM. This is important not just for enabling the global CSS to cascade naturally to
 * the view, but also to allow external tools to scan the document, such as screen readers, search engine bots, activity
 * trackers and automated testing scripts, for example.
 *
 * The view class also brings the MobX dependency for state management.
 */
export abstract class View extends MobxElement  implements BeforeEnterObserver {
  createRenderRoot(): Element | ShadowRoot {
    // Do not use a shadow root
    return this;
  }

  async onBeforeEnter(location: RouterLocation) { 
    this.updatePageMetadata();
  }

  /** Abstract method to update the page metadata when navigating. */
  abstract updatePageMetadata(): void;

}

/**
 * A layout is a container that organizes UI elements in a certain way, and uses shadow root to render its children.
 * <slot> elements can be used to determine where the child elements are rendered.
 *
 * The layout class also bring the MobX dependency for state management.
 */
export class Layout extends MobxElement {
  createRenderRoot(): Element | ShadowRoot {
    // Do not use a shadow root
    return this;
  }

  connectedCallback(): void {
    super.connectedCallback();
  }
}

class JsonLd extends Object {

  serializeJson(): string {
    return "";
  };

  appendOrReplaceToHead() { 
    var script = document.head.querySelector("#search-meta") as HTMLScriptElement;
    if (!script) {
      script = document.createElement("script");
      script.id='search-meta'
      script.type = 'application/ld+json';
      document.head.appendChild(script);
    }
    script.innerText = this.serializeJson();
  }
}

export class PageJsonLd extends JsonLd {
  readonly title: string;
  readonly description: string;
  readonly url: string;

  constructor(title: string,
    description: string,
    url: string) {
        super();
    this.title = title;
    this.description = description;
    this.url = url;
  }

  serializeJson(): string {
    const json = {
      "@context": "http://schema.org",
      "@type": "WebSite",
      "name": this.title,
      "description": this.description,
      "url": this.url,
      "publisher": {
        "@type": "Organization",
        "name": "Vaadin",
        "legalName": "Vaadin Ltd",
        "url": "https://vaadin.com/directory",
      },
      "potentialAction": {
        "@type": "SearchAction",
        "target": {
          "@type": "EntryPoint",
          "urlTemplate": "https://vaadin.com/directory?q={search_term_string}"
        },
        "query-input": "required name=search_term_string"
      }
    }
    return JSON.stringify(json);
  }
}

export class AddonJsonLd extends JsonLd {

  readonly name: string;
  readonly url: string;
  readonly author: string;
  readonly icon: string;
  readonly screenshot: string | null;
  readonly updated: string;
  readonly ratingValue: number | null;
  readonly ratingCount: number | null;

  constructor(
      name: string,
      url: string,
      author: string,
      icon: string,
      screenshot: string | null,
      updated: string,
      ratingValue: number,
      ratingCount: number) {
        super();
        this.name = name;
        this.url = url;
        this.author = author;
        this.icon = icon;
        this.screenshot = screenshot;
        this.updated = updated;
        this.ratingValue = ratingValue;
        this.ratingCount = ratingCount;
    }

    serializeJson(): string {
      const json = {
        "@context": "http://schema.org",
        "@type": "SoftwareApplication",
        "name": this.name,
        "downloadUrl": this.url,
        "image": this.icon,
        "author": {
          "@type": "Person",
          "name": this.author
        },
        "datePublished": this.updated,
        "applicationCategory": "BrowserApplication",
        "screenshot": this.screenshot ? this.screenshot: undefined,
        "aggregateRating": {
          "@type": "AggregateRating",
          "ratingValue": this.ratingValue? this.ratingValue : undefined,
          "ratingCount": this.ratingCount? this.ratingCount : undefined
        }      
      };
      return JSON.stringify(json);
    }

}
