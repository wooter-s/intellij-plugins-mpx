/**
 * 微信小程序组件相关类型定义
 */

export interface WechatComponent {
  /** 组件名称 */
  name: string;
  /** 组件描述 */
  description: string;
  /** 文档 URL */
  docUrl: string;
  /** 组件属性列表 */
  attributes: WechatAttribute[];
  /** 组件事件列表 */
  events: WechatEvent[];
  /** 支持的平台 */
  supportedPlatforms?: string[];
  /** 最低版本要求 */
  minVersion?: string;
}

export interface WechatAttribute {
  /** 属性名 */
  name: string;
  /** 属性类型 */
  type: WechatAttributeType;
  /** 默认值 */
  defaultValue?: string;
  /** 是否必填 */
  required: boolean;
  /** 属性描述 */
  description: string;
  /** 枚举值（如果有） */
  enumValues?: string[];
  /** 是否已废弃 */
  deprecated?: boolean;
  /** 最低版本要求 */
  minVersion?: string;
}

export type WechatAttributeType =
  | 'string'
  | 'number'
  | 'boolean'
  | 'array'
  | 'object'
  | 'function'
  | 'any';

export interface WechatEvent {
  /** 事件名称（不含 bind/catch 前缀） */
  name: string;
  /** 事件描述 */
  description: string;
  /** 事件 detail 字段说明 */
  detail?: Record<string, string>;
}

/**
 * 组件分类
 */
export const COMPONENT_CATEGORIES = [
  { id: 'view', name: '视图容器', path: 'view-container' },
  { id: 'content', name: '基础内容', path: 'basic-content' },
  { id: 'form', name: '表单组件', path: 'form' },
  { id: 'navigation', name: '导航', path: 'navigation' },
  { id: 'media', name: '媒体组件', path: 'media' },
  { id: 'map', name: '地图', path: 'map' },
  { id: 'canvas', name: '画布', path: 'canvas' },
  { id: 'open', name: '开放能力', path: 'open-ability' },
  { id: 'skyline', name: 'Skyline', path: 'skyline' }
] as const;

/**
 * 组件列表（用于直接配置，避免爬取时遗漏）
 */
export const KNOWN_COMPONENTS = [
  // 视图容器
  'view', 'scroll-view', 'swiper', 'swiper-item', 'match-media',
  'movable-area', 'movable-view', 'cover-view', 'cover-image',
  'page-container', 'share-element', 'root-portal', 'grid-view',
  'list-view', 'sticky-header', 'sticky-section', 'nested-scroll-body',
  'nested-scroll-header', 'draggable-sheet', 'grid-builder', 'list-builder',
  'snapshot', 'span', 'open-container',

  // 基础内容
  'text', 'icon', 'progress', 'rich-text',

  // 表单组件
  'button', 'form', 'input', 'textarea', 'label',
  'checkbox', 'checkbox-group', 'radio', 'radio-group',
  'switch', 'slider', 'picker', 'picker-view', 'picker-view-column',
  'editor', 'keyboard-accessory',

  // 导航
  'navigator', 'functional-page-navigator',

  // 媒体组件
  'image', 'video', 'audio', 'camera', 'live-player', 'live-pusher',
  'voip-room', 'lottie', 'channel-live', 'channel-video',

  // 地图
  'map',

  // 画布
  'canvas',

  // 开放能力
  'web-view', 'ad', 'ad-custom', 'official-account', 'open-data',
  'native-component', 'aria-component', 'store-product', 'store-home',

  // Skyline
  'worklet-animation'
] as const;
