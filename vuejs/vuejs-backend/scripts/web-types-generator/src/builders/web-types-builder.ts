import type {
  WebTypes,
  HtmlElement,
  HtmlAttribute,
  VueDirective,
  VueComponent,
  ElementAttribute,
  WxEvent
} from '../types/index.js';

interface BuilderConfig {
  version: string;
  framework?: string;
  name?: string;
}

export class WebTypesBuilder {
  private config: BuilderConfig;
  private elements: HtmlElement[] = [];
  private attributes: HtmlAttribute[] = [];
  private directives: VueDirective[] = [];
  private vueComponents: VueComponent[] = [];
  private wxEvents: WxEvent[] = [];

  constructor(config: BuilderConfig) {
    this.config = {
      framework: 'mpx',
      name: 'mpx',
      ...config
    };
  }

  /**
   * 添加 HTML 元素（组件）
   */
  addElements(elements: HtmlElement[]): this {
    this.elements.push(...elements);
    return this;
  }

  /**
   * 添加 HTML 属性（如 bind:xxx）
   */
  addAttributes(attributes: HtmlAttribute[]): this {
    this.attributes.push(...attributes);
    return this;
  }

  /**
   * 添加 Vue/Mpx 指令
   */
  addDirectives(directives: VueDirective[]): this {
    this.directives.push(...directives);
    return this;
  }

  /**
   * 添加 Vue 组件定义
   */
  addVueComponents(components: VueComponent[]): this {
    this.vueComponents.push(...components);
    return this;
  }

  /**
   * 添加微信事件定义（wx-events 命名空间）
   */
  addWxEvents(events: WxEvent[]): this {
    this.wxEvents.push(...events);
    return this;
  }

  /**
   * 从现有 web-types 合并内容
   */
  mergeExisting(existing: WebTypes): this {
    const html = existing.contributions?.html;

    if (html?.elements) {
      // 只合并不在新数据中的元素
      const newNames = new Set(this.elements.map(e => e.name));
      const toMerge = html.elements.filter(e => !newNames.has(e.name));
      this.elements.push(...toMerge);
    }

    if (html?.attributes) {
      const newNames = new Set(this.attributes.map(a => a.name));
      const toMerge = html.attributes.filter(a => !newNames.has(a.name));
      this.attributes.push(...toMerge);
    }

    if (html?.['vue-directives']) {
      const newNames = new Set(this.directives.map(d => d.name));
      const toMerge = html['vue-directives'].filter(d => !newNames.has(d.name));
      this.directives.push(...toMerge);
    }

    if (html?.['vue-components']) {
      const newNames = new Set(this.vueComponents.map(c => c.name));
      const toMerge = html['vue-components'].filter(c => !newNames.has(c.name));
      this.vueComponents.push(...toMerge);
    }

    if (html?.['wx-events']) {
      const newNames = new Set(this.wxEvents.map(e => e.name));
      const toMerge = html['wx-events'].filter(e => !newNames.has(e.name));
      this.wxEvents.push(...toMerge);
    }

    return this;
  }

  /**
   * 构建最终的 WebTypes 对象
   * 注意：键的顺序很重要，需要与原始文件保持一致以减少 diff
   * 顺序：elements, wx-events, attributes, vue-directives, vue-components
   */
  build(): WebTypes {
    // 合并并去重
    const elements = this.dedupeElements(this.elements);
    const attributes = this.dedupeAttributes(this.attributes);
    const directives = this.dedupeDirectives(this.directives);

    const webTypes: WebTypes = {
      $schema: '../../../../community/platform/webSymbols/web-types-gen/resources/web-types.json',
      framework: this.config.framework!,
      name: this.config.name!,
      version: this.config.version,
      'js-types-syntax': 'typescript',
      'description-markup': 'markdown',
      'default-icon': 'icons/mpx.svg',
      contributions: {
        html: {}
      }
    };

    // 按照原始文件的顺序添加：elements, wx-events, attributes, vue-directives, vue-components
    if (elements.length > 0) {
      webTypes.contributions.html.elements = elements;
    }

    // wx-events 放在 elements 之后（与原始文件顺序一致）
    if (this.wxEvents.length > 0) {
      webTypes.contributions.html['wx-events'] = this.wxEvents;
    }

    if (attributes.length > 0) {
      webTypes.contributions.html.attributes = attributes;
    }

    if (directives.length > 0) {
      webTypes.contributions.html['vue-directives'] = directives;
    }

    if (this.vueComponents.length > 0) {
      webTypes.contributions.html['vue-components'] = this.vueComponents;
    }

    return webTypes;
  }

  private dedupeElements(elements: HtmlElement[]): HtmlElement[] {
    const map = new Map<string, HtmlElement>();

    for (const element of elements) {
      const existing = map.get(element.name);

      if (existing) {
        // 合并属性
        existing.attributes = this.mergeElementAttributes(
          existing.attributes || [],
          element.attributes || []
        );

        // 合并事件
        if (element.events) {
          existing.events = [
            ...(existing.events || []),
            ...element.events.filter(
              e => !existing.events?.some(ex => ex.name === e.name)
            )
          ];
        }

        // 如果新元素有描述而旧的没有，使用新描述
        if (!existing.description && element.description) {
          existing.description = element.description;
        }
      } else {
        map.set(element.name, { ...element });
      }
    }

    // 按名称排序
    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }

  private mergeElementAttributes(
    existing: ElementAttribute[],
    incoming: ElementAttribute[]
  ): ElementAttribute[] {
    const map = new Map(existing.map(a => [a.name, a]));

    for (const attr of incoming) {
      if (!map.has(attr.name)) {
        map.set(attr.name, attr);
      }
    }

    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }

  private dedupeAttributes(attributes: HtmlAttribute[]): HtmlAttribute[] {
    const map = new Map<string, HtmlAttribute>();

    for (const attr of attributes) {
      if (!map.has(attr.name)) {
        map.set(attr.name, attr);
      }
    }

    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }

  private dedupeDirectives(directives: VueDirective[]): VueDirective[] {
    const map = new Map<string, VueDirective>();

    for (const directive of directives) {
      if (!map.has(directive.name)) {
        map.set(directive.name, directive);
      }
    }

    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }
}
