import type { ElementAttribute, VueFileTopElement } from '../types/index.js';

const stringAttribute = (name: string): ElementAttribute => ({
  name,
  value: {
    kind: 'plain',
    type: 'string',
    required: false
  }
});

const booleanAttribute = (name: string): ElementAttribute => ({
  name,
  value: {
    kind: 'no-value'
  }
});

export function getMpxTopLevelElements(): VueFileTopElement[] {
  return [
    {
      name: 'template',
      attributes: [
        stringAttribute('lang'),
        stringAttribute('src'),
        booleanAttribute('functional')
      ]
    },
    {
      name: 'script',
      attributes: [
        stringAttribute('lang'),
        stringAttribute('id'),
        stringAttribute('src'),
        booleanAttribute('setup'),
        {
          name: 'name',
          value: {
            kind: 'plain',
            type: 'enum',
            required: false
          },
          values: [{ name: 'json' }]
        }
      ]
    },
    {
      name: 'style',
      attributes: [
        stringAttribute('lang'),
        stringAttribute('src'),
        stringAttribute('module'),
        booleanAttribute('scoped')
      ]
    }
  ];
}
