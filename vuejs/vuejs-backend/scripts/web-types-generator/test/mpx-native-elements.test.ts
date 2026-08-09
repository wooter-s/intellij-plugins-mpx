import assert from 'node:assert/strict';
import test from 'node:test';

import { WebTypesBuilder } from '../src/builders/web-types-builder.js';
import { getMpxNativeElements } from '../src/data/mpx-native-elements.js';
import type { WebTypes } from '../src/types/index.js';

test('MPX native elements expose the missing WXML tags and attributes', () => {
  const elements = getMpxNativeElements();
  const byName = new Map(elements.map(element => [element.name, element]));

  assert.deepEqual(
    [...byName.keys()].sort(),
    ['block', 'import', 'include', 'navigation-bar', 'page-meta', 'qs', 'template', 'wxs']
  );
  assert.equal(byName.get('import')?.attributes?.find(attribute => attribute.name === 'src')?.required, true);
  assert.deepEqual(
    byName.get('wxs')?.attributes?.map(attribute => attribute.name),
    ['module', 'src']
  );
  assert.deepEqual(
    byName.get('template')?.attributes?.map(attribute => attribute.name),
    ['name', 'is', 'data']
  );
});

test('native template attributes extend the existing Vue template definition', () => {
  const existing: WebTypes = {
    $schema: 'test',
    framework: 'mpx',
    name: 'mpx',
    version: 'test',
    'js-types-syntax': 'typescript',
    'description-markup': 'markdown',
    'default-icon': 'icons/mpx.svg',
    contributions: {
      html: {
        elements: [
          {
            name: 'template',
            extension: true,
            attributes: [
              {
                name: 'scope',
                value: { kind: 'plain', type: 'string' }
              }
            ]
          }
        ],
        'vue-file-top-elements': [
          { name: 'template' },
          { name: 'script' },
          { name: 'style' }
        ]
      }
    }
  };

  const webTypes = new WebTypesBuilder({ version: 'test' })
    .mergeExisting(existing)
    .addElements(getMpxNativeElements())
    .build();
  const templates = webTypes.contributions.html.elements
    ?.filter(element => element.name === 'template') ?? [];

  assert.equal(templates.length, 1);
  assert.deepEqual(
    templates[0].attributes?.map(attribute => attribute.name),
    ['data', 'is', 'name', 'scope']
  );
  assert.equal(
    templates[0]['doc-url'],
    'https://developers.weixin.qq.com/miniprogram/dev/reference/wxml/template.html'
  );
  assert.deepEqual(
    webTypes.contributions.html['vue-file-top-elements']?.map(element => element.name).sort(),
    ['script', 'style', 'template']
  );
});
