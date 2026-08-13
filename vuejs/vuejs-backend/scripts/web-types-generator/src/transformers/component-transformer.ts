import type {
  WechatComponent,
  WechatAttribute,
  HtmlElement,
  ElementAttribute,
  AttributeValue,
  ElementEvent
} from '../types/index.js';

export class ComponentTransformer {
  transform(component: WechatComponent): HtmlElement {
    return {
      name: component.name,
      description: component.description || undefined,
      'doc-url': component.docUrl,
      attributes: component.attributes.map(attr => this.transformAttribute(attr)),
      'exclusive-contributions': ['/js/events'],
      js: component.events.length > 0
        ? { events: component.events.map(evt => this.transformEvent(evt)) }
        : undefined
    };
  }

  transformAll(components: WechatComponent[]): HtmlElement[] {
    return components.map(c => this.transform(c));
  }

  private transformAttribute(attr: WechatAttribute): ElementAttribute {
    const result: ElementAttribute = {
      name: attr.name,
      description: attr.description || undefined,
      value: this.transformValue(attr)
    };

    if (attr.enumValues && attr.enumValues.length > 0) {
      result.values = attr.enumValues.map(name => ({ name }));
    }

    if (attr.deprecated) {
      result.deprecated = true;
    }

    // Do not map the documentation's `required` column to Web Types. Several
    // rows are renderer-dependent or only describe a meaningful runtime value
    // (for example text.overflow and input.placeholder). Web Types would treat
    // them as globally required and report false-positive missing attributes.

    if (attr.defaultValue) {
      result.default = attr.defaultValue;
    }

    return result;
  }

  private transformValue(attr: WechatAttribute): AttributeValue {
    if (attr.enumValues && attr.enumValues.length > 0) {
      return {
        kind: 'plain',
        type: 'enum'
      };
    }

    return {
      kind: 'plain',
      type: this.typeName(attr.type)
    };
  }

  private typeName(type: WechatAttribute['type']): string {
    switch (type) {
      case 'array':
        return 'any[]';
      case 'function':
        return 'function';
      case 'object':
        return 'object';
      case 'boolean':
      case 'number':
      case 'string':
        return type;
      default:
        return 'any';
    }
  }

  private transformEvent(evt: { name: string; description: string }): ElementEvent {
    return {
      name: evt.name,
      description: evt.description || undefined,
      value: {
        kind: 'plain',
        type: 'function'
      }
    };
  }
}
