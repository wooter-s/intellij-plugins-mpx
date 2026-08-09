import type {
  MpxDirectiveConfig,
  VueDirective,
  HtmlAttribute,
  AttributeValue
} from '../types/index.js';

export class DirectiveTransformer {
  private directives: MpxDirectiveConfig[];

  constructor(directives: MpxDirectiveConfig[]) {
    this.directives = directives;
  }

  /**
   * 转换为 wx: 指令命名空间
   */
  transformWxDirectives(): VueDirective[] {
    return this.directives.map(directive => this.transformDirective(directive));
  }

  private transformDirective(config: MpxDirectiveConfig): VueDirective {
    const result: VueDirective = {
      name: config.name,
      description: config.description
    };

    if (config.docUrl) {
      result['doc-url'] = config.docUrl;
    }

    if (config.aliases && config.aliases.length > 0) {
      result.aliases = config.aliases;
    }

    // 设置属性值
    result['attribute-value'] = this.transformValue(config);

    // 处理修饰符
    if (config.modifiers && config.modifiers.length > 0) {
      result['vue-modifiers'] = config.modifiers.map(mod => ({
        name: mod.name,
        description: mod.description
      }));
    }

    return result;
  }

  private transformValue(config: MpxDirectiveConfig): AttributeValue {
    if (config.noValue) {
      return { kind: 'no-value' };
    }

    if (config.isExpression) {
      return {
        kind: 'expression',
        type: config.valueType === 'void' ? 'void' : undefined,
        required: config.required !== false
      };
    }

    return {
      kind: 'plain',
      type: config.valueType === 'array' ? 'any[]' : config.valueType ?? 'any',
      required: config.required !== false
    };
  }

  /**
   * 生成 MPX 全局属性模式：wx 指令、事件绑定以及 mark/model 属性。
   */
  transformGlobalAttributes(): HtmlAttribute[] {
    const prefixes = [
      { name: 'bind', description: '绑定事件处理函数。支持 bind:tap 或 bindtap 两种写法。' },
      { name: 'capture-bind', description: '捕获阶段绑定事件处理函数。支持 capture-bind:tap 或 capture-bindtap 两种写法。' },
      { name: 'capture-catch', description: '捕获阶段绑定事件并阻止冒泡。支持 capture-catch:tap 或 capture-catchtap 两种写法。' },
      { name: 'catch', description: '绑定事件处理函数，并阻止事件冒泡。支持 catch:tap 或 catchtap 两种写法。' },
      { name: 'mut-bind', description: '互斥事件绑定，用于解决多层嵌套组件的事件冒泡问题。支持 mut-bind:tap 或 mut-bindtap 两种写法。' }
    ];

    const customEvent = {
      name: 'Custom event',
      pattern: '[\\w-]+',
      priority: 'lowest',
      'doc-hide-pattern': true,
      'hide-from-completion': true
    };
    const attributes: HtmlAttribute[] = [
      {
        name: 'WeChat directive',
        description: '微信小程序及 MPX 的 wx 指令。',
        pattern: {
          items: '/html/wx-directives',
          template: [
            'wx',
            { or: [':', '-'] },
            '#...',
            '#item:directive'
          ]
        }
      }
    ];

    for (const prefix of prefixes) {
      attributes.push({
        name: prefix.name,
        description: prefix.description,
        'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/framework/view/wxml/event.html',
        pattern: {
          items: '/js/events',
          template: [
            prefix.name,
            {
              template: [':'],
              required: false
            },
            '#...',
            '#item:event'
          ]
        },
        value: {
          kind: 'plain',
          type: 'function',
          required: true
        },
        js: {
          events: [customEvent]
        }
      });
    }

    for (const prefix of ['mark', 'model']) {
      attributes.push({
        name: `${prefix} attribute`,
        description: `${prefix}:* 是 MPX 的普通属性前缀，不是事件绑定。`,
        pattern: {
          template: [
            `${prefix}:`,
            { regex: '[\\w-]+' }
          ]
        },
        value: {
          kind: 'plain',
          type: 'any'
        }
      });
    }

    return attributes;
  }
}
