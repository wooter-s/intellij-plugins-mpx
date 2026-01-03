import type { MpxDirectiveConfig } from '../types/index.js';

/**
 * Mpx 增强指令配置
 * 这些指令是 Mpx 框架对微信小程序的增强，需要手动维护
 */
export const MPX_DIRECTIVES: MpxDirectiveConfig[] = [
  // 条件渲染
  {
    name: 'if',
    description: '条件渲染。根据表达式的值的 truthy/falsy 来有条件地渲染元素。当条件变化时该元素及其内部指令/组件会被销毁与重建。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/conditional.html',
    isExpression: true,
    valueType: 'boolean'
  },
  {
    name: 'elif',
    description: '表示 wx:if 的"else if 块"。必须跟在 wx:if 或 wx:elif 元素后面。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/conditional.html',
    isExpression: true,
    valueType: 'boolean'
  },
  {
    name: 'else',
    description: '表示 wx:if 的"else 块"。必须跟在 wx:if 或 wx:elif 元素后面。不需要值。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/conditional.html',
    noValue: true
  },

  // 列表渲染
  {
    name: 'for',
    description: '基于源数据多次渲染元素或模板块。使用特殊语法 wx:for-item 和 wx:for-index 来指定迭代变量名。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/list.html',
    isExpression: true,
    valueType: 'array'
  },
  {
    name: 'for-item',
    description: '指定数组当前元素的变量名，默认为 item。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/list.html',
    isExpression: false,
    required: false
  },
  {
    name: 'for-index',
    description: '指定数组当前下标的变量名，默认为 index。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/list.html',
    isExpression: false,
    required: false
  },
  {
    name: 'key',
    description: '为列表渲染的元素指定唯一的 key，用于优化渲染性能。推荐使用稳定且唯一的值，如 id。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/list.html',
    isExpression: false,
    required: false
  },

  // Mpx 增强指令
  {
    name: 'model',
    description: 'Mpx 增强指令。在表单控件或组件上创建双向数据绑定。类似 Vue 的 v-model。',
    docUrl: 'https://mpxjs.cn/guide/basic/two-way-bindind.html',
    isExpression: true,
    modifiers: [
      { name: 'lazy', description: '在 change 而非 input 事件后更新数据' },
      { name: 'number', description: '将用户输入自动转换为数字' },
      { name: 'trim', description: '自动过滤用户输入的首尾空白字符' }
    ]
  },
  {
    name: 'model-prop',
    description: 'Mpx 增强指令。指定 wx:model 绑定的属性名，默认为 value。',
    docUrl: 'https://mpxjs.cn/guide/basic/two-way-binding.html',
    isExpression: false,
    required: false
  },
  {
    name: 'model-event',
    description: 'Mpx 增强指令。指定 wx:model 监听的事件名，默认为 input。',
    docUrl: 'https://mpxjs.cn/guide/basic/two-way-binding.html',
    isExpression: false,
    required: false
  },
  {
    name: 'model-value-path',
    description: 'Mpx 增强指令。指定从事件对象中获取值的路径，默认为 value。',
    docUrl: 'https://mpxjs.cn/guide/basic/two-way-binding.html',
    isExpression: false,
    required: false
  },
  {
    name: 'model-filter',
    description: 'Mpx 增强指令。指定 wx:model 的过滤函数，用于在更新数据前处理值。',
    docUrl: 'https://mpxjs.cn/guide/basic/two-way-binding.html',
    isExpression: true,
    required: false
  },
  {
    name: 'class',
    description: 'Mpx 增强指令。动态绑定 class。支持对象语法和数组语法，类似 Vue 的 :class。',
    docUrl: 'https://mpxjs.cn/guide/basic/class-bindind.html',
    isExpression: true
  },
  {
    name: 'style',
    description: 'Mpx 增强指令。动态绑定内联样式。支持对象语法和数组语法，类似 Vue 的 :style。',
    docUrl: 'https://mpxjs.cn/guide/basic/style-binding.html',
    isExpression: true
  },
  {
    name: 'show',
    description: 'Mpx 增强指令。根据表达式的真假值，切换元素的 display CSS 属性。与 wx:if 不同，元素始终存在于 DOM 中。',
    docUrl: 'https://mpxjs.cn/guide/basic/conditional-render.html',
    isExpression: true,
    valueType: 'boolean'
  },
  {
    name: 'ref',
    description: 'Mpx 增强指令。为元素或子组件注册引用信息。引用信息将会在父组件的 $refs 对象上注册。',
    docUrl: 'https://mpxjs.cn/guide/basic/refs.html',
    isExpression: false
  },

  // 模板相关
  {
    name: 'name',
    description: '定义模板的名称，用于 template 元素。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/template.html',
    isExpression: false
  },
  {
    name: 'is',
    description: '动态选择要使用的模板名称。用于 template 元素。',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/template.html',
    isExpression: true
  },

  // 动态组件
  {
    name: 'component',
    description: 'Mpx 增强指令。用于渲染动态组件。类似于 Vue 的动态组件功能。',
    docUrl: 'https://mpxjs.cn/guide/advance/dynamic-component.html',
    isExpression: true
  }
];

/**
 * 导出为可用于 JSON 序列化的格式
 */
export function getMpxDirectivesConfig(): MpxDirectiveConfig[] {
  return MPX_DIRECTIVES;
}
