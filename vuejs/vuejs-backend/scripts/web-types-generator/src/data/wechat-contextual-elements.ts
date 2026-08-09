import type { HtmlElement } from '../types/index.js';

/**
 * Attributes documented by WeChat in another component's usage section rather
 * than in the component's own property table.
 */
export function getWechatContextualElements(): HtmlElement[] {
  return [
    {
      name: 'cover-view',
      attributes: [
        {
          name: 'marker-id',
          description: '在 map 的 callout 插槽中绑定对应的 marker',
          'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/component/map.html#marker-上的自定义气泡-customCallout',
          value: {
            kind: 'plain',
            type: 'number'
          }
        }
      ]
    }
  ];
}
