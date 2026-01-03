import { load, CheerioAPI } from 'cheerio';
import { fetchHtml, logger } from '../utils/index.js';
import type {
  WechatComponent,
  WechatAttribute,
  WechatAttributeType,
  WechatEvent
} from '../types/index.js';
import { KNOWN_COMPONENTS } from '../types/wechat.js';

const BASE_URL = 'https://developers.weixin.qq.com/miniprogram/dev/component';

interface ExtractorOptions {
  useCache?: boolean;
  components?: string[];
}

export class WechatDocsExtractor {
  private options: ExtractorOptions;

  constructor(options: ExtractorOptions = {}) {
    this.options = {
      useCache: true,
      components: [...KNOWN_COMPONENTS],
      ...options
    };
  }

  async extract(): Promise<WechatComponent[]> {
    const components: WechatComponent[] = [];
    const componentNames = this.options.components ?? [...KNOWN_COMPONENTS];

    logger.info(`Extracting ${componentNames.length} components from WeChat docs...`);

    for (let i = 0; i < componentNames.length; i++) {
      const name = componentNames[i];
      logger.step(i + 1, componentNames.length, `Extracting: ${name}`);

      try {
        const component = await this.extractComponent(name);
        if (component) {
          components.push(component);
        }
      } catch (error) {
        logger.warn(`Failed to extract component: ${name}`, error);
      }
    }

    logger.success(`Extracted ${components.length} components`);
    return components;
  }

  private async extractComponent(name: string): Promise<WechatComponent | null> {
    const url = `${BASE_URL}/${name}.html`;

    try {
      const html = await fetchHtml(url, { useCache: this.options.useCache });
      const $ = load(html);

      const description = this.extractDescription($);
      const attributes = this.extractAttributes($);
      const events = this.extractEvents($);

      return {
        name,
        description,
        docUrl: url,
        attributes,
        events
      };
    } catch (error) {
      logger.debug(`Component not found or error: ${name}`, error);
      return null;
    }
  }

  private extractDescription($: CheerioAPI): string {
    // 提取第一段描述
    const desc = $('.page-content > p').first().text().trim();
    return desc || '';
  }

  private extractAttributes($: CheerioAPI): WechatAttribute[] {
    const attributes: WechatAttribute[] = [];

    // 查找属性表格
    $('table').each((_, table) => {
      const $table = $(table);
      const headers = $table
        .find('thead th, tr:first-child th, tr:first-child td')
        .map((_, th) => $(th).text().trim().toLowerCase())
        .get();

      // 检查是否是属性表格
      const hasAttrColumn = headers.some(h =>
        h.includes('属性') || h.includes('property') || h.includes('name')
      );
      const hasTypeColumn = headers.some(h =>
        h.includes('类型') || h.includes('type')
      );

      if (!hasAttrColumn || !hasTypeColumn) {
        return;
      }

      // 确定列索引
      const nameIdx = headers.findIndex(h =>
        h.includes('属性') || h.includes('property') || h === 'name'
      );
      const typeIdx = headers.findIndex(h =>
        h.includes('类型') || h.includes('type')
      );
      const defaultIdx = headers.findIndex(h =>
        h.includes('默认') || h.includes('default')
      );
      const requiredIdx = headers.findIndex(h =>
        h.includes('必填') || h.includes('required')
      );
      const descIdx = headers.findIndex(h =>
        h.includes('说明') || h.includes('description') || h.includes('备注')
      );

      // 解析表格行
      $table.find('tbody tr, tr:not(:first-child)').each((_, tr) => {
        const cells = $(tr).find('td').map((_, td) => $(td).text().trim()).get();

        if (cells.length === 0) return;

        const name = nameIdx >= 0 ? cells[nameIdx] : cells[0];
        if (!name || name === '属性') return; // Skip header row if in tbody

        const typeStr = typeIdx >= 0 ? cells[typeIdx] : '';
        const defaultVal = defaultIdx >= 0 ? cells[defaultIdx] : undefined;
        const requiredStr = requiredIdx >= 0 ? cells[requiredIdx] : '';
        const description = descIdx >= 0 ? cells[descIdx] : cells[cells.length - 1] || '';

        // 解析枚举值
        const enumValues = this.parseEnumValues(typeStr, description);

        attributes.push({
          name: this.normalizeAttributeName(name),
          type: this.parseType(typeStr),
          defaultValue: defaultVal && defaultVal !== '-' ? defaultVal : undefined,
          required: requiredStr.includes('是') || requiredStr.toLowerCase() === 'true',
          description: this.cleanDescription(description),
          enumValues: enumValues.length > 0 ? enumValues : undefined,
          deprecated: description.includes('已废弃') || description.includes('deprecated')
        });
      });
    });

    return attributes;
  }

  private extractEvents($: CheerioAPI): WechatEvent[] {
    const events: WechatEvent[] = [];

    // 查找事件表格（通常在属性表格之后）
    $('table').each((_, table) => {
      const $table = $(table);
      const headers = $table
        .find('thead th, tr:first-child th')
        .map((_, th) => $(th).text().trim().toLowerCase())
        .get();

      // 检查是否是事件表格
      const hasEventColumn = headers.some(h =>
        h.includes('事件') || h.includes('event')
      );

      if (!hasEventColumn) {
        return;
      }

      const nameIdx = headers.findIndex(h =>
        h.includes('事件') || h.includes('event') || h === 'name'
      );
      const descIdx = headers.findIndex(h =>
        h.includes('说明') || h.includes('description')
      );

      $table.find('tbody tr').each((_, tr) => {
        const cells = $(tr).find('td').map((_, td) => $(td).text().trim()).get();

        if (cells.length === 0) return;

        const rawName = nameIdx >= 0 ? cells[nameIdx] : cells[0];
        const description = descIdx >= 0 ? cells[descIdx] : cells[cells.length - 1] || '';

        // 提取事件名（去掉 bind 前缀）
        const name = this.extractEventName(rawName);
        if (!name) return;

        events.push({
          name,
          description: this.cleanDescription(description)
        });
      });
    });

    return events;
  }

  private normalizeAttributeName(name: string): string {
    // 移除文档中可能的标记
    return name
      .replace(/\s*\(.*\)/, '')
      .replace(/\s+/g, '')
      .trim();
  }

  private parseType(typeStr: string): WechatAttributeType {
    const normalized = typeStr.toLowerCase().trim();

    if (normalized.includes('boolean') || normalized === 'bool') {
      return 'boolean';
    }
    if (normalized.includes('number') || normalized === 'float' || normalized === 'integer') {
      return 'number';
    }
    if (normalized.includes('array')) {
      return 'array';
    }
    if (normalized.includes('object')) {
      return 'object';
    }
    if (normalized.includes('function') || normalized.includes('eventhandle')) {
      return 'function';
    }
    if (normalized.includes('string')) {
      return 'string';
    }

    return 'any';
  }

  private parseEnumValues(typeStr: string, description: string): string[] {
    const values: string[] = [];

    // 从类型字符串解析：如 "left/center/right"
    if (typeStr.includes('/')) {
      const parts = typeStr.split('/').map(s => s.trim()).filter(Boolean);
      if (parts.every(p => !p.includes(' '))) {
        values.push(...parts);
      }
    }

    // 从描述中解析：如 "可选值：default, primary, warn"
    const enumMatch = description.match(/可选值[：:]\s*([^。]+)/);
    if (enumMatch) {
      const enumStr = enumMatch[1];
      const parts = enumStr.split(/[,，、]/).map(s => s.trim()).filter(Boolean);
      values.push(...parts);
    }

    return [...new Set(values)]; // Remove duplicates
  }

  private extractEventName(rawName: string): string | null {
    // 移除 bind/catch 前缀
    let name = rawName
      .replace(/^(bind|catch|capture-bind|capture-catch|mut-bind):?/, '')
      .trim();

    // 过滤无效名称
    if (!name || name === '事件' || name === 'event') {
      return null;
    }

    return name;
  }

  private cleanDescription(desc: string): string {
    return desc
      .replace(/\s+/g, ' ')
      .replace(/。+$/, '。')
      .trim();
  }
}
