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
      events: component.events.length > 0
        ? component.events.map(evt => this.transformEvent(evt))
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

    if (attr.deprecated) {
      result.deprecated = true;
    }

    if (attr.required) {
      result.required = true;
    }

    if (attr.defaultValue) {
      result.default = attr.defaultValue;
    }

    return result;
  }

  private transformValue(attr: WechatAttribute): AttributeValue {
    // 布尔类型使用表达式
    if (attr.type === 'boolean') {
      return {
        kind: 'expression',
        type: [{ name: true }, { name: false }]
      };
    }

    // 有枚举值时使用 plain
    if (attr.enumValues && attr.enumValues.length > 0) {
      return {
        kind: 'plain',
        type: attr.enumValues.map(v => ({ name: v }))
      };
    }

    // 函数类型使用表达式（事件处理器）
    if (attr.type === 'function') {
      return {
        kind: 'expression',
        type: []
      };
    }

    // 对象和数组使用表达式
    if (attr.type === 'object' || attr.type === 'array') {
      return {
        kind: 'expression',
        type: []
      };
    }

    // 默认使用 plain
    return {
      kind: 'plain',
      type: []
    };
  }

  private transformEvent(evt: { name: string; description: string }): ElementEvent {
    return {
      name: evt.name,
      description: evt.description || undefined
    };
  }
}
