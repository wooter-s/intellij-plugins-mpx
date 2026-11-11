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
  valueType?: 'plain' | 'void' | 'any' | 'boolean' | 'array';
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

/**
 * 事件描述映射
 */
export const KNOWN_EVENTS_DESCRIPTIONS: Record<string, string> = {
  // 通用事件
  'tap': '触摸后马上离开',
  'touchstart': '触摸开始',
  'touchmove': '触摸后移动',
  'touchend': '触摸结束',
  'touchcancel': '触摸被取消（如来电打断）',
  'longpress': '长按（超过350ms）',
  'longtap': '长按（已废弃，推荐使用 longpress）',
  'transitionend': 'CSS transition 动画结束',
  'animationstart': 'CSS animation 动画开始',
  'animationiteration': 'CSS animation 动画一次迭代结束',
  'animationend': 'CSS animation 动画结束',
  'touchforcechange': '3D Touch 按压力度变化',

  // 表单事件
  'input': '输入时触发',
  'focus': '获得焦点时触发',
  'blur': '失去焦点时触发',
  'confirm': '点击完成按钮时触发',
  'change': '值改变时触发',
  'submit': '表单提交时触发',
  'reset': '表单重置时触发',

  // 滚动事件
  'scroll': '滚动时触发',
  'scrolltoupper': '滚动到顶部/左边时触发',
  'scrolltolower': '滚动到底部/右边时触发',
  'refresherpulling': '下拉刷新时触发',
  'refresherrefresh': '下拉刷新被触发',
  'refresherrestore': '下拉刷新被复位',
  'refresherabort': '下拉刷新被中止',

  // 媒体事件
  'play': '开始播放时触发',
  'pause': '暂停播放时触发',
  'ended': '播放结束时触发',
  'timeupdate': '播放进度变化时触发',
  'fullscreenchange': '全屏状态变化时触发',
  'waiting': '缓冲中时触发',
  'error': '发生错误时触发',
  'progress': '加载进度变化时触发',
  'loadedmetadata': '元数据加载完成时触发',

  // 其他事件
  'load': '加载完成时触发',
  'ready': '组件准备就绪时触发',
  'show': '组件显示时触发',
  'hide': '组件隐藏时触发',
  'resize': '尺寸变化时触发'
};
