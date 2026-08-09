import type { AttributeValueOption, ElementAttribute, HtmlElement } from '../types/index.js';

interface AttributeOptions {
  type?: string;
  required?: boolean;
  defaultValue?: string;
  values?: AttributeValueOption[];
}

const attribute = (
  name: string,
  description: string,
  options: AttributeOptions = {}
): ElementAttribute => ({
  name,
  description,
  value: {
    kind: 'plain',
    type: options.type ?? 'string'
  },
  required: options.required || undefined,
  default: options.defaultValue,
  values: options.values
});

const enumValues = (...names: string[]): AttributeValueOption[] =>
  names.map(name => ({ name }));

export function getMpxNativeElements(): HtmlElement[] {
  return [
    {
      name: 'block',
      description: '仅作为逻辑渲染容器，不会在页面中渲染额外节点。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/list.html'
    },
    {
      name: 'import',
      description: '导入目标 WXML 文件中定义的 template。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/import.html',
      attributes: [
        attribute('src', '导入的 WXML 文件路径。', { required: true })
      ]
    },
    {
      name: 'include',
      description: '引入目标 WXML 文件中除 template 与 WXS 外的内容。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/import.html',
      attributes: [
        attribute('src', '要引入的 WXML 文件路径。', { required: true })
      ]
    },
    {
      name: 'wxs',
      description: '在 WXML 中声明或引入 WXS 脚本模块。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/framework/view/wxs/',
      attributes: [
        attribute('module', 'WXS 模块名称。'),
        attribute('src', '引入的 WXS 文件路径。')
      ]
    },
    {
      name: 'qs',
      description: '在模板中声明或引入 QS 脚本模块。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/framework/view/wxs/',
      attributes: [
        attribute('module', 'QS 模块名称。'),
        attribute('src', '引入的 QS 文件路径。')
      ]
    },
    {
      name: 'page-meta',
      description: '配置页面属性；只能作为页面模板内的第一个节点。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/component/page-meta.html',
      attributes: [
        attribute('background-text-style', '下拉背景字体和 loading 图的样式。', {
          type: 'enum',
          values: enumValues('dark', 'light')
        }),
        attribute('background-color', '窗口背景颜色。'),
        attribute('background-color-top', '顶部窗口背景颜色，仅 iOS 支持。'),
        attribute('background-color-bottom', '底部窗口背景颜色，仅 iOS 支持。'),
        attribute('scroll-top', '页面滚动位置，支持 px 或 rpx。'),
        attribute('scroll-duration', '滚动动画时长，单位为毫秒。', {
          type: 'number',
          defaultValue: '300'
        }),
        attribute('page-style', '页面根节点样式。'),
        attribute('root-font-size', '页面根字体大小，用作 rem 单位的基准。')
      ]
    },
    {
      name: 'navigation-bar',
      description: '配置页面导航栏；需要作为 page-meta 的第一个子节点。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/component/navigation-bar.html',
      attributes: [
        attribute('title', '导航栏标题。'),
        attribute('loading', '是否显示 loading 加载提示。', {
          type: 'boolean',
          defaultValue: 'false'
        }),
        attribute('front-color', '导航栏前景颜色。', {
          type: 'enum',
          values: enumValues('#ffffff', '#000000')
        }),
        attribute('background-color', '导航栏背景颜色。'),
        attribute('color-animation-duration', '导航栏颜色动画时长，单位为毫秒。', {
          type: 'number',
          defaultValue: '0'
        }),
        attribute('color-animation-timing-func', '导航栏颜色动画方式。', {
          type: 'enum',
          defaultValue: 'linear',
          values: enumValues('linear', 'easeIn', 'easeOut', 'easeInOut')
        })
      ]
    },
    {
      name: 'template',
      extension: true,
      description: '定义或使用可复用的 WXML 模板。',
      'doc-url': 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/template.html',
      attributes: [
        attribute('name', '定义模板时使用的名称。'),
        attribute('is', '要使用的模板名称。'),
        attribute('data', '传入模板的数据。', { type: 'any' })
      ]
    }
  ];
}
