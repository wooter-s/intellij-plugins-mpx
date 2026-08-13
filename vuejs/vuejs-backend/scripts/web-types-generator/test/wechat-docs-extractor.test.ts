import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

import {
  assertAllComponentsExtracted,
  assertNoComponentMetadataRegression,
  WechatDocsExtractor
} from '../src/extractors/wechat-docs-extractor.js';
import { ComponentTransformer } from '../src/transformers/component-transformer.js';
import { DirectiveTransformer } from '../src/transformers/directive-transformer.js';
import { getWechatContextualElements } from '../src/data/wechat-contextual-elements.js';
import { WebTypesBuilder } from '../src/builders/web-types-builder.js';
import { KNOWN_COMPONENTS } from '../src/types/wechat.js';
import type { WebTypes } from '../src/types/web-types.js';

const fixture = (name: string): Promise<string> =>
  readFile(new URL(`./fixtures/${name}.html`, import.meta.url), 'utf-8');

// Snapshot of the ordinary component pages listed by the WeChat documentation.
// page-meta and navigation-bar are maintained as static WXML extensions instead.
const CURRENT_WECHAT_COMPONENT_SNAPSHOT = [
  'view', 'scroll-view', 'swiper', 'swiper-item', 'match-media',
  'movable-area', 'movable-view', 'cover-view', 'cover-image',
  'page-container', 'share-element', 'root-portal', 'grid-view',
  'list-view', 'sticky-header', 'sticky-section', 'nested-scroll-body',
  'nested-scroll-header', 'draggable-sheet', 'grid-builder', 'list-builder',
  'snapshot', 'span', 'open-container',
  'double-tap-gesture-handler', 'force-press-gesture-handler',
  'horizontal-drag-gesture-handler', 'long-press-gesture-handler',
  'pan-gesture-handler', 'scale-gesture-handler', 'tap-gesture-handler',
  'vertical-drag-gesture-handler',
  'text', 'icon', 'progress', 'rich-text',
  'button', 'form', 'input', 'textarea', 'label',
  'checkbox', 'checkbox-group', 'radio', 'radio-group',
  'switch', 'slider', 'picker', 'picker-view', 'picker-view-column',
  'editor', 'editor-portal', 'keyboard-accessory', 'selection',
  'navigator', 'functional-page-navigator',
  'image', 'video', 'audio', 'camera', 'live-player', 'live-pusher',
  'voip-room', 'channel-live', 'channel-video',
  'map', 'canvas',
  'web-view', 'ad', 'ad-custom', 'official-account', 'official-account-publish',
  'open-data', 'open-data-item', 'open-data-list',
  'native-component', 'aria-component', 'store-product', 'store-home',
  'store-coupon', 'store-gift'
] as const;

test('map extraction keeps component rows and turns eventhandle rows into events', async () => {
  const extractor = new WechatDocsExtractor({ components: [] });
  const component = extractor.extractFromHtml('map', await fixture('map'));

  assert.deepEqual(
    component.attributes.map(attribute => attribute.name),
    ['markers', 'show-location', 'enable-zoom']
  );
  assert.equal(component.attributes[0].description, '标记点');
  assert.equal(component.attributes.some(attribute => attribute.name === 'iconPath'), false);
  assert.deepEqual(component.events.map(event => event.name), ['regionchange']);

  const element = new ComponentTransformer().transform(component);
  assert.deepEqual(element.attributes?.find(attribute => attribute.name === 'markers')?.value, {
    kind: 'plain',
    type: 'any[]'
  });
  assert.deepEqual(element.attributes?.find(attribute => attribute.name === 'show-location')?.value, {
    kind: 'plain',
    type: 'boolean'
  });
  assert.equal(element.js?.events?.[0].name, 'regionchange');
  assert.deepEqual(element['exclusive-contributions'], ['/js/events']);
});

test('picker extraction includes mode sections and excludes return/value tables', async () => {
  const extractor = new WechatDocsExtractor({ components: [] });
  const component = extractor.extractFromHtml('picker', await fixture('picker'));

  assert.deepEqual(
    component.attributes.map(attribute => attribute.name),
    ['mode', 'range']
  );
});

test('configured component list matches the reviewed WeChat index snapshot', () => {
  assert.deepEqual(
    [...KNOWN_COMPONENTS].sort(),
    [...CURRENT_WECHAT_COMPONENT_SNAPSHOT].sort()
  );
});

test('generated Web Types contains exactly the reviewed official component pages', async () => {
  const webTypes = JSON.parse(
    await readFile(
      new URL('../../../resources/web-types/wx@0.0.0.web-types.json', import.meta.url),
      'utf-8'
    )
  ) as WebTypes;
  const actualNames = (webTypes.contributions.html.elements ?? [])
    .filter(element => element['doc-url']?.startsWith(
      'https://developers.weixin.qq.com/miniprogram/dev/component/'
    ))
    .map(element => element.name)
    .sort();

  assert.deepEqual(
    actualNames,
    [...CURRENT_WECHAT_COMPONENT_SNAPSHOT, 'navigation-bar', 'page-meta'].sort()
  );

  const globallyRequiredAttributes = (webTypes.contributions.html.elements ?? [])
    .filter(element => element['doc-url']?.startsWith(
      'https://developers.weixin.qq.com/miniprogram/dev/component/'
    ))
    .flatMap(element => (element.attributes ?? [])
      .filter(attribute => attribute.required === true)
      .map(attribute => `${element.name}.${attribute.name}`));
  assert.deepEqual(globallyRequiredAttributes, []);
});

test('generated wx and MPX Web Types have distinct identities and elements', async () => {
  const [wxWebTypes, mpxWebTypes] = await Promise.all([
    readFile(
      new URL('../../../resources/web-types/wx@0.0.0.web-types.json', import.meta.url),
      'utf-8'
    ).then(content => JSON.parse(content) as WebTypes),
    readFile(
      new URL('../../../resources/web-types/vue@3.4.0.web-types.json', import.meta.url),
      'utf-8'
    ).then(content => JSON.parse(content) as WebTypes)
  ]);

  assert.equal(wxWebTypes.framework, undefined);
  assert.deepEqual(wxWebTypes['required-context'], { kind: 'framework', name: 'mpx' });
  assert.equal(wxWebTypes.name, 'wx');
  assert.equal(wxWebTypes.version, '0.0.0');
  assert.equal(wxWebTypes['default-icon'], 'icons/wx.svg');

  const wxNames = new Set(
    (wxWebTypes.contributions.html.elements ?? []).map(element => element.name)
  );
  const mpxNames = new Set(
    (mpxWebTypes.contributions.html.elements ?? []).map(element => element.name)
  );

  assert.equal(wxNames.size, CURRENT_WECHAT_COMPONENT_SNAPSHOT.length + 7);
  assert.deepEqual(
    ['block', 'import', 'include', 'template', 'wxs', 'page-meta', 'navigation-bar']
      .filter(name => !wxNames.has(name)),
    []
  );
  assert.deepEqual([...wxNames].filter(name => mpxNames.has(name)), []);
  assert.deepEqual([...mpxNames].sort(), ['Vue component', 'qs', 'select', 'slot'].sort());
});

test('component documentation requirements are not emitted as global Web Types requirements', () => {
  const element = new ComponentTransformer().transform({
    name: 'text',
    description: '文本',
    docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/component/text.html',
    attributes: [
      {
        name: 'overflow',
        type: 'string',
        required: true,
        description: 'Skyline 文本溢出处理'
      }
    ],
    events: []
  });

  assert.equal(element.attributes?.[0].required, undefined);
});

test('worklet eventhandler rows remain direct attributes', () => {
  const extractor = new WechatDocsExtractor({ components: [] });
  const component = extractor.extractFromHtml(
    'pan-gesture-handler',
    `
      <article><h1 id="pan-gesture-handler">pan-gesture-handler</h1>
        <p>拖动手势组件</p>
        <h2>属性说明</h2>
        <table>
          <thead><tr><th>属性</th><th>类型</th><th>默认值</th><th>必填</th><th>说明</th></tr></thead>
          <tbody>
            <tr><td>worklet:ongesture</td><td>eventhandler</td><td></td><td>否</td><td>手势回调</td></tr>
            <tr><td>bindchange</td><td>eventhandler</td><td></td><td>否</td><td>状态变化</td></tr>
          </tbody>
        </table>
      </article>
    `
  );

  assert.deepEqual(component.attributes.map(attribute => attribute.name), ['worklet:ongesture']);
  assert.deepEqual(component.events.map(event => event.name), ['change']);

  const element = new ComponentTransformer().transform(component);
  assert.deepEqual(
    element.attributes?.find(attribute => attribute.name === 'worklet:ongesture')?.value,
    { kind: 'plain', type: 'function' }
  );
  assert.equal(element.js?.events?.some(event => event.name === 'worklet:ongesture'), false);
});

test('soft error pages are rejected even when HTTP succeeds', () => {
  const extractor = new WechatDocsExtractor({ components: [] });

  assert.throws(
    () => extractor.extractFromHtml(
      'text',
      '<html><title>访问验证</title><body><h1>请完成验证</h1><p>请求过于频繁</p></body></html>'
    ),
    /Invalid WeChat component document for text: component heading not found/
  );
});

test('expected component extraction fails when any configured component is missing', () => {
  assert.throws(
    () => assertAllComponentsExtracted(
      ['view', 'text', 'selection'],
      [
        { name: 'view', description: '', docUrl: '', attributes: [], events: [] },
        { name: 'text', description: '', docUrl: '', attributes: [], events: [] }
      ]
    ),
    /Failed to extract expected WeChat components: selection/
  );
});

test('existing non-empty component metadata cannot be replaced with empty data', () => {
  const existing = new WebTypesBuilder({ version: 'test' })
    .addElements([
      {
        name: 'text',
        attributes: [{ name: 'user-select' }],
        js: { events: [{ name: 'tap' }] }
      }
    ])
    .build();

  assert.throws(
    () => assertNoComponentMetadataRegression(existing, [
      {
        name: 'text',
        description: '文本',
        docUrl: 'https://developers.weixin.qq.com/miniprogram/dev/component/text.html',
        attributes: [],
        events: []
      }
    ]),
    /text lost 1 attributes; text lost 1 events/
  );
});

test('contextual map attributes are merged into cover-view', () => {
  const webTypes = new WebTypesBuilder({ version: 'test' })
    .addElements([
      {
        name: 'cover-view',
        attributes: [
          {
            name: 'scroll-top',
            value: { kind: 'plain', type: 'number' }
          }
        ]
      },
      ...getWechatContextualElements()
    ])
    .build();

  const coverView = webTypes.contributions.html.elements
    ?.find(element => element.name === 'cover-view');
  assert.deepEqual(
    coverView?.attributes?.map(attribute => attribute.name),
    ['marker-id', 'scroll-top']
  );
});

test('wx expression directives keep expression values', () => {
  const directives = new DirectiveTransformer([
    {
      name: 'if',
      description: '条件渲染',
      isExpression: true,
      valueType: 'boolean'
    }
  ]).transformWxDirectives();

  assert.deepEqual(directives[0]['attribute-value'], {
    kind: 'expression',
    type: undefined,
    required: true
  });
});
