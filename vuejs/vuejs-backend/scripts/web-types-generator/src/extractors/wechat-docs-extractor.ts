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

interface ExtractedProperties {
  attributes: WechatAttribute[];
  events: WechatEvent[];
}

const STANDARD_PROPERTY_SECTIONS = new Set([
  '属性说明',
  '通用属性',
  'Skyline 特有属性',
  'WebView 特有属性'
]);

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
      return this.extractFromHtml(name, html, url);
    } catch (error) {
      logger.debug(`Component not found or error: ${name}`, error);
      return null;
    }
  }

  extractFromHtml(name: string, html: string, docUrl = `${BASE_URL}/${name}.html`): WechatComponent {
    const $ = load(html);
    const { attributes, events } = this.extractProperties($, name);
    return {
      name,
      description: this.extractDescription($),
      docUrl,
      attributes,
      events
    };
  }

  private extractDescription($: CheerioAPI): string {
    // 提取第一段描述（尝试多种选择器）
    let desc = $('article > p').first().text().trim();
    if (!desc) {
      desc = $('section > p').first().text().trim();
    }
    if (!desc) {
      desc = $('p').first().text().trim();
    }
    return desc || '';
  }

  private extractProperties($: CheerioAPI, componentName: string): ExtractedProperties {
    const attributes: WechatAttribute[] = [];
    const events: WechatEvent[] = [];
    const propertyTables = this.findPropertyTables($, componentName);

    for (const $table of propertyTables) {
      const headers = this.extractHeaders($table, $);

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

      // 解析表格行（只处理直接子行，避免嵌套表格的行）
      const $tbody = $table.children('tbody');
      const $rows = $tbody.length > 0
        ? $tbody.children('tr')
        : $table.children('tr').slice(1);

      $rows.each((_, tr) => {
        const $tr = $(tr);

        if ($tr.children('th').length > 0) {
          return;
        }

        const cells = $tr.children('td').map((_, td) => {
          const $td = $(td);
          const $nestedTable = $td.find('table');
          if ($nestedTable.length > 0) {
            const $clone = $td.clone();
            $clone.find('table').remove();
            return $clone.text().trim();
          }
          return $td.text().trim();
        }).get();

        if (cells.length === 0) return;

        const name = nameIdx >= 0 ? cells[nameIdx] : cells[0];
        if (!name || name === '属性' || name === '合法值') return;

        const typeStr = typeIdx >= 0 ? cells[typeIdx] : '';
        const defaultVal = defaultIdx >= 0 ? cells[defaultIdx] : undefined;
        const requiredStr = requiredIdx >= 0 ? cells[requiredIdx] : '';
        const description = descIdx >= 0 ? cells[descIdx] : cells[cells.length - 1] || '';
        const type = this.parseType(typeStr);
        const normalizedName = this.normalizeAttributeName(name);

        const $descTd = $tr.children('td').eq(descIdx >= 0 ? descIdx : cells.length - 1);
        const enumValues = this.parseEnumValuesFromCell($descTd, $, typeStr, description);

        if (type === 'function') {
          const eventName = this.extractEventName(normalizedName);
          if (eventName) {
            events.push({
              name: eventName,
              description: this.cleanDescription(description)
            });
          }
          return;
        }

        attributes.push({
          name: normalizedName,
          type,
          defaultValue: defaultVal && defaultVal !== '-' ? defaultVal : undefined,
          required: requiredStr.includes('是') || requiredStr.toLowerCase() === 'true',
          description: this.cleanDescription(description),
          enumValues: enumValues.length > 0 ? enumValues : undefined,
          deprecated: description.includes('已废弃') || description.includes('deprecated')
        });
      });
    }

    return {
      attributes: this.dedupeByName(attributes),
      events: this.dedupeByName(events)
    };
  }

  private findPropertyTables(
    $: CheerioAPI,
    componentName: string
  ): ReturnType<CheerioAPI>[] {
    const candidates: Array<{
      table: ReturnType<CheerioAPI>;
      heading: string;
    }> = [];

    $('table').each((_, table) => {
      const $table = $(table);
      if ($table.parents('td').length > 0) {
        return;
      }
      const headers = this.extractHeaders($table, $);
      if (!this.isPropertyTable(headers)) {
        return;
      }
      candidates.push({
        table: $table,
        heading: this.findTableHeading($table)
      });
    });

    const selected = candidates.filter(({ heading }) =>
      STANDARD_PROPERTY_SECTIONS.has(heading)
      || (componentName === 'picker' && /\bmode\s*=/i.test(heading))
    );

    if (selected.length > 0) {
      return selected.map(({ table }) => table);
    }

    if (candidates.length > 0) {
      logger.warn(
        `No recognized property section for ${componentName}; falling back to the first property table.`
      );
      return [candidates[0].table];
    }

    return [];
  }

  private extractHeaders(
    $table: ReturnType<CheerioAPI>,
    $: CheerioAPI
  ): string[] {
    const $thead = $table.children('thead');
    if ($thead.length > 0) {
      return $thead.find('> tr > th, > tr > td')
        .map((_, th) => $(th).text().trim().toLowerCase())
        .get();
    }

    const $firstRow = $table.children('tbody').children('tr').first();
    const $headers = $firstRow.length > 0
      ? $firstRow.children('th, td')
      : $table.children('tr').first().children('th, td');
    return $headers
      .map((_, th) => $(th).text().trim().toLowerCase())
      .get();
  }

  private isPropertyTable(headers: string[]): boolean {
    return headers.some(h =>
      h.includes('属性') || h.includes('property') || h === 'name'
    ) && headers.some(h =>
      h.includes('类型') || h.includes('type')
    );
  }

  private findTableHeading($table: ReturnType<CheerioAPI>): string {
    let current = $table;
    for (let depth = 0; depth < 5; depth++) {
      const $heading = current.prevAll('h1, h2, h3, h4, h5, h6').first();
      if ($heading.length > 0) {
        return $heading.text().replace(/^#+\s*/, '').trim();
      }
      current = current.parent();
    }
    return '';
  }

  private parseEnumValuesFromCell($cell: ReturnType<CheerioAPI>, $: CheerioAPI, typeStr: string, description: string): string[] {
    const values: string[] = [];

    // 从嵌套表格中提取枚举值
    const $nestedTable = $cell.find('table');
    if ($nestedTable.length > 0) {
      $nestedTable.find('tbody tr, tr').each((_, tr) => {
        const firstCell = $(tr).find('td').first().text().trim();
        if (firstCell && firstCell !== '合法值' && firstCell !== '说明') {
          values.push(firstCell);
        }
      });
    }

    // 如果没有从嵌套表格获取，尝试从类型字符串和描述中解析
    if (values.length === 0) {
      values.push(...this.parseEnumValues(typeStr, description));
    }

    return [...new Set(values)]; // Remove duplicates
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

  private dedupeByName<T extends { name: string }>(items: T[]): T[] {
    const byName = new Map<string, T>();
    for (const item of items) {
      if (!byName.has(item.name)) {
        byName.set(item.name, item);
      }
    }
    return [...byName.values()];
  }
}
