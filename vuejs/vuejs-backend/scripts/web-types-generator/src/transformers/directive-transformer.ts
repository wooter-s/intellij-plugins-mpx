import type {
  MpxDirectiveConfig,
  VueDirective,
  HtmlAttribute,
  AttributeValue,
  VueModifier
} from '../types/index.js';
import { KNOWN_EVENTS } from '../types/mpx-directive.js';

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
   * 生成事件绑定属性（bind:xxx, catch:xxx 等）
   */
  transformBindDirectives(): HtmlAttribute[] {
    const prefixes = [
      { name: 'bind', description: '绑定事件处理函数' },
      { name: 'catch', description: '绑定事件处理函数，并阻止事件冒泡' },
      { name: 'mut-bind', description: '互斥事件绑定，用于解决多层嵌套组件的事件冒泡问题' },
      { name: 'capture-bind', description: '捕获阶段绑定事件处理函数' },
      { name: 'capture-catch', description: '捕获阶段绑定事件并阻止冒泡' }
    ];

    const attributes: HtmlAttribute[] = [];

    for (const prefix of prefixes) {
      // 创建带模式的属性
      const attr: HtmlAttribute = {
        name: prefix.name,
        description: prefix.description,
        'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/framework/view/wxml/event.html',
        'vue-argument': {
          description: '事件名称',
          pattern: {
            items: KNOWN_EVENTS.map(event => ({
              name: event,
              description: `${event} 事件`
            }))
          },
          required: true
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
