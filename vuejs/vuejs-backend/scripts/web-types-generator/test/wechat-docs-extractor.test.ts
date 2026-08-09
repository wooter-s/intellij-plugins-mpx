import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

import { WechatDocsExtractor } from '../src/extractors/wechat-docs-extractor.js';
import { ComponentTransformer } from '../src/transformers/component-transformer.js';
import { DirectiveTransformer } from '../src/transformers/directive-transformer.js';
import { getWechatContextualElements } from '../src/data/wechat-contextual-elements.js';
import { WebTypesBuilder } from '../src/builders/web-types-builder.js';

const fixture = (name: string): Promise<string> =>
  readFile(new URL(`./fixtures/${name}.html`, import.meta.url), 'utf-8');

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
