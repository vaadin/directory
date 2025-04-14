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
  private messages: Message[] = [];

  @state()
  private totalPosts: Number = 0;

  async firstUpdated() {
    if (this.addon) {
      try {
        this.messages = await DiscussionEndpoint.listFirstMessages(this.addon, 5, {mute: true}) || [];

        this.totalPosts = this.messages.length > 0 ? this.messages[0].postCount : 0;

        // Fetch rest of the messages async, delay to avoid blocking the UI
        setTimeout(() => {
            DiscussionEndpoint.listMessages(this.addon, {mute: true})
                .then(newMessages => this.messages = newMessages);
        }, 1000);
      } catch (error) {
        console.error('Error fetching messages:', error);
      }
    }
  }

  createRenderRoot() {
    return this; // disable shadow DOM
  }

  render() {
      const intro = html`
            <div class="discussions-intro">
              <a href="${this.discourseUrl}c/directory/${this.addon ? this.addon : '' }" class="discussions-button">
                  Give feedback or ask questions
              </a>
              <p>Total ${this.totalPosts} posts</p>
            </div>`;
       if (!this.messages || this.messages.length < 1) {
         return html`
              ${intro}
              <div class="discussion-messages">
              </div>`
        }
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
             ${this.totalPosts > 5 && this.messages.length == 5  ? html`<i>Loading messages...</i>` : html``}
          </div>
        <a href="${this.discourseUrl}c/directory/${this.addon ? this.addon : '' }" class="discussions-button">
            More questions of feedback?
        </a>

        `;
      }
}
