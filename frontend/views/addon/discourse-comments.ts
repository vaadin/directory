import {LitElement, html} from 'lit';
import moment from 'moment';
import {customElement, property, state} from 'lit/decorators.js';
import {classMap} from 'lit/directives/class-map.js';

import { DiscussionEndpoint } from 'Frontend/generated/endpoints';

import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import DomPurify from 'dompurify';

// Import the Message type from the same location used in discussions.tsx
import { Message } from 'Frontend/generated/org/vaadin/directory/endpoint/discussion/DiscussionEndpoint/Message';

@customElement('discourse-comments')
class DiscourseComments extends LitElement {

  @property()
  addon?: string;

  @property()
  discourseUrl?: string;

  @state()
  private totalPosts: number = 0;

  @state()
  private hasDiscussions: boolean = true;

  @state()
  private messages: Message[] = [];


  async firstUpdated() {
    if (this.addon) {
      try {
        this.messages = await DiscussionEndpoint.listFirstMessages(this.addon, 5, {mute: true}) || [];

        // Set the total posts count from the first message
        this.totalPosts = this.messages.length > 0 ? this.messages[0].postCount : 0;

        // Fetch rest of the messages async, delay to avoid blocking the UI
        setTimeout(() => {
            DiscussionEndpoint.listMessages(this.addon, { mute: true })
              .then((newMessages: Message[]) => {
                this.messages = newMessages;
              this.totalPosts = newMessages.length > 0 ? newMessages[0].postCount : 0;
              if (this.totalPosts <= 0) {
                DiscussionEndpoint.discussionExists(this.addon, {mute: true}).then((exists:boolean) => {
                  this.hasDiscussions = exists || this.totalPosts > 0;
                });
              }
              });
        }, 1000);
      } catch (error) {
        console.error('Error fetching messages:', error);
      }
    }
  }

  createRenderRoot() {
    return this; // disable shadow DOM
  }

  initDiscussionThreadIfNeeded() {
    if (this.addon) {
      // Change the mouse cursor to "waiting"
      document.body.style.cursor = 'wait';

      DiscussionEndpoint.createDiscussionIfNeeded(this.addon, { mute: true })
        .then((url: string) => window.open(url));
    }
  }

  render() {
    // Show the intro message and the messages
      const intro = html`
            <div class="discussions-intro">
             <a @click=${this.initDiscussionThreadIfNeeded} class="discussions-button">
                  Give feedback or ask questions</a>
              <p>Total ${this.totalPosts} posts</p>
            </div>`;

        // Render messages. If there are more than 20 messages, show the link in the end
        return html`
          ${intro}
          <div class="discussion-messages">
            ${this.messages.map((message) => html`
              <div class="message ${classMap({reply: message.reply})}">
                <div class="message-header">
                  <img src=${message.imageUrl} />
                  <span class="message-author">${message.author}</span>
                  <span class="date" title="${message.date+' '+message.time}" >${moment(message.date+' '+message.time).fromNow()}</span>
                </div>
                <p>${unsafeHTML(DomPurify.sanitize(message.text || ""))}</p>
                <a href="${this.discourseUrl}t/${message.topicId}" class="reply-btn">Reply</a>
              </div>
            `)}
             ${this.totalPosts > 5 && this.messages.length == 5  ? html`<p></P><i>Loading messages...</i></p>` : html``}
          </div>
          ${this.messages.length > 20 ? html`
            <a href="${this.discourseUrl}c/directory/${this.addon ? this.addon : '' }" class="discussions-button">
              More questions of feedback?
            </a>` : html``}

        `;
      }
}
