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

  private extractAttributes($: CheerioAPI): WechatAttribute[] {
    const attributes: WechatAttribute[] = [];

    // 查找属性表格（只处理顶层表格，跳过嵌套表格）
    $('table').each((_, table) => {
      const $table = $(table);

      // 跳过嵌套在 td 中的表格（如"合法值"枚举表格）
      if ($table.parents('td').length > 0) {
        return;
      }

      // 使用直接子选择器获取表头，避免获取嵌套表格的表头
      let headers: string[] = [];
      const $thead = $table.children('thead');
      if ($thead.length > 0) {
        headers = $thead.find('> tr > th, > tr > td')
          .map((_, th) => $(th).text().trim().toLowerCase())
          .get();
      } else {
        // 如果没有 thead，使用第一行作为表头
        const $firstRow = $table.children('tbody').children('tr').first();
        if ($firstRow.length === 0) {
          const $directFirstRow = $table.children('tr').first();
          headers = $directFirstRow.children('th, td')
            .map((_, th) => $(th).text().trim().toLowerCase())
            .get();
        } else {
          headers = $firstRow.children('th, td')
            .map((_, th) => $(th).text().trim().toLowerCase())
            .get();
        }
      }

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

      // 解析表格行（只处理直接子行，避免嵌套表格的行）
      const $tbody = $table.children('tbody');
      const $rows = $tbody.length > 0
        ? $tbody.children('tr')
        : $table.children('tr').slice(1); // 跳过第一行（表头）

      $rows.each((_, tr) => {
        const $tr = $(tr);

        // 跳过表头行
        if ($tr.children('th').length > 0) {
          return;
        }

        // 只获取直接子 td，避免获取嵌套表格的 td
        const cells = $tr.children('td').map((_, td) => {
          // 获取 td 的直接文本，忽略嵌套表格的内容
          const $td = $(td);
          const $nestedTable = $td.find('table');
          if ($nestedTable.length > 0) {
            // 如果有嵌套表格，只获取嵌套表格之前的文本
            const $clone = $td.clone();
            $clone.find('table').remove();
            return $clone.text().trim();
          }
          return $td.text().trim();
        }).get();

        if (cells.length === 0) return;

        const name = nameIdx >= 0 ? cells[nameIdx] : cells[0];
        if (!name || name === '属性' || name === '合法值') return; // Skip header row or enum value rows

        const typeStr = typeIdx >= 0 ? cells[typeIdx] : '';
        const defaultVal = defaultIdx >= 0 ? cells[defaultIdx] : undefined;
        const requiredStr = requiredIdx >= 0 ? cells[requiredIdx] : '';
        const description = descIdx >= 0 ? cells[descIdx] : cells[cells.length - 1] || '';

        // 解析枚举值（从嵌套表格中提取）
        const $descTd = $tr.children('td').eq(descIdx >= 0 ? descIdx : cells.length - 1);
        const enumValues = this.parseEnumValuesFromCell($descTd, $, typeStr, description);

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

  private extractEvents($: CheerioAPI): WechatEvent[] {
    const events: WechatEvent[] = [];

    // 查找事件表格（只处理顶层表格，跳过嵌套表格）
    $('table').each((_, table) => {
      const $table = $(table);

      // 跳过嵌套在 td 中的表格
      if ($table.parents('td').length > 0) {
        return;
      }

      // 使用直接子选择器获取表头
      let headers: string[] = [];
      const $thead = $table.children('thead');
      if ($thead.length > 0) {
        headers = $thead.find('> tr > th, > tr > td')
          .map((_, th) => $(th).text().trim().toLowerCase())
          .get();
      } else {
        const $firstRow = $table.children('tbody').children('tr').first();
        if ($firstRow.length === 0) {
          const $directFirstRow = $table.children('tr').first();
          headers = $directFirstRow.children('th, td')
            .map((_, th) => $(th).text().trim().toLowerCase())
            .get();
        } else {
          headers = $firstRow.children('th, td')
            .map((_, th) => $(th).text().trim().toLowerCase())
            .get();
        }
      }

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

      // 解析表格行（只处理直接子行）
      const $tbody = $table.children('tbody');
      const $rows = $tbody.length > 0
        ? $tbody.children('tr')
        : $table.children('tr').slice(1);

      $rows.each((_, tr) => {
        const $tr = $(tr);

        // 跳过表头行
        if ($tr.children('th').length > 0) {
          return;
        }

        const cells = $tr.children('td').map((_, td) => $(td).text().trim()).get();

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
