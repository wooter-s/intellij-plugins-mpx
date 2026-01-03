/**
 * Mpx 指令配置类型定义
 */

export interface MpxDirectiveConfig {
  /** 指令名称（不含 wx: 前缀） */
  name: string;
  /** 指令描述 */
  description: string;
  /** 文档 URL */
  docUrl?: string;
  /** 是否为表达式类型 */
  isExpression?: boolean;
  /** 是否无值 (如 wx:else) */
  noValue?: boolean;
  /** 值类型 */
  valueType?: 'plain' | 'void' | 'any';
  /** 是否必填 */
  required?: boolean;
  /** 支持的修饰符 */
  modifiers?: MpxModifier[];
  /** 别名 */
  aliases?: string[];
}

export interface MpxModifier {
  /** 修饰符名称 */
  name: string;
  /** 修饰符描述 */
  description: string;
}

/**
 * 绑定指令配置
 */
export interface BindDirectiveConfig {
  /** 绑定前缀 (bind, catch, mut-bind, capture-bind, capture-catch) */
  prefix: string;
  /** 描述 */
  description: string;
  /** 支持的事件列表 */
  events?: string[];
}

/**
 * 已知事件列表（用于 bind/catch 补全）
 */
export const KNOWN_EVENTS = [
  // 通用事件
  'tap', 'touchstart', 'touchmove', 'touchend', 'touchcancel',
  'longpress', 'longtap', 'transitionend', 'animationstart',
  'animationiteration', 'animationend', 'touchforcechange',

  // 表单事件
  'input', 'focus', 'blur', 'confirm', 'change', 'submit', 'reset',

  // 滚动事件
  'scroll', 'scrolltoupper', 'scrolltolower', 'refresherpulling',
  'refresherrefresh', 'refresherrestore', 'refresherabort',

  // 媒体事件
  'play', 'pause', 'ended', 'timeupdate', 'fullscreenchange',
  'waiting', 'error', 'progress', 'loadedmetadata',

  // 其他事件
  'load', 'ready', 'show', 'hide', 'resize'
] as const;
