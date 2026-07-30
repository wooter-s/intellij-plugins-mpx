/**
 * Web-types JSON 结构定义
 * @see https://github.com/ArkBriar/webTypes/blob/master/README.md
 */

export interface WebTypes {
  $schema: string;
  framework: string;
  name: string;
  version: string;
  'js-types-syntax': string;
  'description-markup': string;
  'default-icon': string;
  contributions: Contributions;
}

export interface Contributions {
  html: HtmlContributions;
  css?: CssContributions;
}

export interface HtmlContributions {
  elements?: HtmlElement[];
  attributes?: HtmlAttribute[];
  'vue-directives'?: VueDirective[];
  'vue-file-top-elements'?: VueFileTopElement[];
  'vue-components'?: VueComponent[];
  'wx-events'?: WxEvent[];
}

export interface WxEvent {
  name: string;
  description: string;
}

export interface CssContributions {
  properties?: CssProperty[];
}

export interface HtmlElement {
  name: string;
  description?: string;
  'doc-url'?: string;
  attributes?: ElementAttribute[];
  events?: ElementEvent[];
  slots?: ElementSlot[];
  exclusive?: boolean;
  extension?: boolean;
  'pattern'?: HtmlPattern;
}

export interface ElementAttribute {
  name: string;
  deprecated?: boolean;
  required?: boolean;
  default?: string;
  description?: string;
  'doc-url'?: string;
  value?: AttributeValue;
  'exclusive-contributions'?: string[];
}

export interface AttributeValue {
  kind: 'plain' | 'expression' | 'no-value';
  type?: AttributeValueType[] | string;
  required?: boolean;
}

export interface AttributeValueType {
  name: string | boolean;
}

export interface ElementEvent {
  name: string;
  description?: string;
  arguments?: EventArgument[];
}

export interface EventArgument {
  name: string;
  type?: string;
}

export interface ElementSlot {
  name: string;
  description?: string;
}

export interface HtmlAttribute {
  name: string;
  description?: string;
  'doc-url'?: string;
  virtual?: boolean;
  value?: AttributeValue;
  'exclusive-contributions'?: string[];
  pattern?: HtmlPattern;
  argument?: HtmlArgument;
  'vue-argument'?: VueArgument;
  'vue-modifiers'?: VueModifier[];
}

export interface HtmlArgument {
  name: string;
  description?: string;
  required?: boolean;
  pattern?: HtmlPattern;
}

export interface VueArgument {
  description?: string;
  pattern?: string | HtmlPattern;
  required?: boolean;
}

export interface VueModifier {
  name: string;
  description?: string;
  pattern?: HtmlPattern;
}

export interface HtmlPattern {
  items?: string | PatternItem[];
  regex?: string;
  template?: (string | PatternTemplate)[];
  delegate?: string;
  or?: string[];
  proximity?: number;
}

export interface PatternItem {
  path?: string;
  name?: string;
  description?: string;
  'symbol-name-match'?: string;
}

export interface PatternTemplate {
  items?: PatternItem | PatternItem[] | string;
  priority?: string;
  required?: boolean;
  'repeat'?: boolean;
  unique?: boolean;
  'proximity'?: number;
  delegate?: string;
  template?: string[];
  or?: string[];
}

export interface VueDirective {
  name: string;
  aliases?: string[];
  description?: string;
  'doc-url'?: string;
  'attribute-value'?: AttributeValue;
  'vue-argument'?: VueArgument;
  'vue-modifiers'?: VueModifier[];
}

export interface VueFileTopElement {
  name: string;
  description?: string;
  attributes?: ElementAttribute[];
}

export interface VueComponent {
  name: string;
  aliases?: string[];
  description?: string;
  'doc-url'?: string;
  props?: ComponentProp[];
  slots?: ElementSlot[];
  events?: ElementEvent[];
  source?: ComponentSource;
}

export interface ComponentProp {
  name: string;
  type?: string;
  required?: boolean;
  default?: string;
  description?: string;
}

export interface ComponentSource {
  module?: string;
  symbol?: string;
}

export interface CssProperty {
  name: string;
  description?: string;
  values?: string[];
}
