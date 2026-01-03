import type {
  MpxDirectiveConfig,
  VueDirective,
  HtmlAttribute,
  AttributeValue,
  VueModifier,
  WxEvent
} from '../types/index.js';
import { KNOWN_EVENTS, KNOWN_EVENTS_DESCRIPTIONS } from '../types/mpx-directive.js';

export class DirectiveTransformer {
  private directives: MpxDirectiveConfig[];

  constructor(directives: MpxDirectiveConfig[]) {
    this.directives = directives;
  }

  /**
   * 转换为 wx: 指令（vue-directives 格式）
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
      type: [],
      required: config.required !== false
    };
  }

  /**
   * 生成 wx-events 命名空间（事件列表）
   */
  generateWxEvents(): WxEvent[] {
    return KNOWN_EVENTS.map(event => ({
      name: event,
      description: KNOWN_EVENTS_DESCRIPTIONS[event] || `${event} 事件`
    }));
  }

  /**
   * 生成 bind alias 属性（事件绑定的合并语法支持）
   * 例如: bindtap, bind:tap, catchtap, catch:tap 等
   */
  generateBindAlias(): HtmlAttribute {
    return {
      name: 'bind alias',
      description: 'Dynamically bind one or more attributes, or a component prop to an expression. Supports merged syntax (bindtap) and colon syntax (bind:tap).',
      'doc-url': 'https://v3.vuejs.org/api/directives.html#v-bind',
      virtual: true,
      value: {
        kind: 'expression',
        type: 'any',
        required: true
      },
      'exclusive-contributions': ['/html/modifiers'],
      pattern: {
        proximity: 10,
        template: [
          {
            or: ['bind', 'mut-bind', 'capture-bind', 'catch', 'capture-catch']
          },
          {
            or: ['', ':']
          },
          {
            items: '/html/wx-events'
          }
        ]
      }
    };
  }

  /**
   * 生成事件绑定属性（bind:xxx, catch:xxx 等）
   * 使用 argument 格式，支持 bind:tap 和 bindtap 两种写法
   */
  transformBindDirectives(): HtmlAttribute[] {
    const prefixes = [
      { name: 'bind', description: '绑定事件处理函数。支持 bind:tap 或 bindtap 两种写法。' },
      { name: 'capture-bind', description: '捕获阶段绑定事件处理函数。支持 capture-bind:tap 或 capture-bindtap 两种写法。' },
      { name: 'capture-catch', description: '捕获阶段绑定事件并阻止冒泡。支持 capture-catch:tap 或 capture-catchtap 两种写法。' },
      { name: 'catch', description: '绑定事件处理函数，并阻止事件冒泡。支持 catch:tap 或 catchtap 两种写法。' },
      { name: 'mut-bind', description: '互斥事件绑定，用于解决多层嵌套组件的事件冒泡问题。支持 mut-bind:tap 或 mut-bindtap 两种写法。' }
    ];

    const attributes: HtmlAttribute[] = [];

    for (const prefix of prefixes) {
      const attr: HtmlAttribute = {
        name: prefix.name,
        description: prefix.description,
        'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/framework/view/wxml/event.html',
        pattern: {
          template: [
            '#item',
            {
              delegate: 'argument',
              template: [':', '#item:event name'],
              required: false
            }
          ]
        },
        argument: {
          name: 'event name',
          description: '事件名称',
          required: true,
          pattern: {
            items: '/html/wx-events'
          }
        },
        value: {
          kind: 'expression',
          type: 'void',
          required: true
        }
      };

      attributes.push(attr);
    }

    return attributes;
  }
}
