import { Filter } from './Filter';

export class FilterAddedEvent extends Event {
  filter: Filter;

  constructor(filter: Filter) {
    super('filter-added', { bubbles: true, composed: true });
    this.filter = filter;
  }
}
