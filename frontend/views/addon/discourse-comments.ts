import { LitElement } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';

@customElement('discourse-comments')
export class DiscourseComments extends LitElement {
  @property({ type: String }) discourseUrl = '';
  @property({ type: String }) discourseEmbedUrl = '';
  @property({ type: String }) discourseUserName = '';
  @property({ type: String }) discourseReferrerPolicy = 'no-referrer-when-downgrade';
  @property({ type: Number }) topicId?: number;
  @property({ type: String }) className?: string;

  iframe?: HTMLIFrameElement;

  createRenderRoot() {
    return this; // disable shadow DOM
  }

  connectedCallback() {
    super.connectedCallback();
    if (!this.discourseUrl || !this.discourseEmbedUrl.startsWith('http')) {
      console.error('Invalid discourseUrl or discourseEmbedUrl');
      return;
    }

    const params = new URLSearchParams({
      embed_url: encodeURIComponent(this.discourseEmbedUrl),
    });
    if (this.discourseUserName) params.set('discourse_username', this.discourseUserName);
    if (this.topicId) params.set('topic_id', this.topicId.toString());
    if (this.className) params.set('class_name', this.className);

    const src = `${this.discourseUrl}embed/comments?${params.toString()}`;

    this.iframe = document.createElement('iframe');
    this.iframe.src = src;
    this.iframe.id = 'discourse-embed-frame';
    this.iframe.width = '100%';
    this.iframe.frameBorder = '0';
    this.iframe.scrolling = 'no';
    this.iframe.referrerPolicy = this.discourseReferrerPolicy;

    this.appendChild(this.iframe);
    window.addEventListener('message', this.onMessage, false);
  }

  disconnectedCallback() {
    window.removeEventListener('message', this.onMessage);
    super.disconnectedCallback();
  }

  private onMessage = (e: MessageEvent) => {
    if (!this.iframe || !e.origin.includes(this._normalizeUrl(this.discourseUrl))) return;

    if (e.data?.type === 'discourse-resize' && e.data.height) {
      this.iframe.height = `${e.data.height}px`;
    }

    if (e.data?.type === 'discourse-scroll' && e.data.top) {
      window.scrollTo(0, this._getOffsetTop(this.iframe) + e.data.top);
    }
  };

  private _normalizeUrl(url: string) {
    return url.replace(/^https?:\/\//, '');
  }

  private _getOffsetTop(el: HTMLElement): number {
    let top = 0;
    while (el) {
      top += el.offsetTop;
      el = el.offsetParent as HTMLElement;
    }
    return top;
  }
}