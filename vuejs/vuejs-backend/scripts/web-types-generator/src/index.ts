#!/usr/bin/env node
import { Command } from 'commander';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import {
  assertNoComponentMetadataRegression,
  WechatDocsExtractor
} from './extractors/index.js';
import { ComponentTransformer, DirectiveTransformer } from './transformers/index.js';
import { WebTypesBuilder } from './builders/index.js';
import {
  getMpxDirectivesConfig,
  getMpxNativeElements,
  getMpxTopLevelElements,
  getWechatContextualElements,
  getWechatNativeElements
} from './data/index.js';
import { logger } from './utils/logger.js';
import { clearCache } from './utils/cache.js';
import type { WebTypes } from './types/index.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// 默认输出路径
const DEFAULT_OUTPUT_PATH = resolve(
  __dirname,
  '../../../resources/web-types/vue@3.4.0.web-types.json'
);
const WX_OUTPUT_FILENAME = 'wx@0.0.0.web-types.json';

interface GenerateOptions {
  dryRun: boolean;
  noCache: boolean;
  output: string;
  wxOutput?: string;
  verbose: boolean;
}

async function loadExistingWebTypes(path: string): Promise<WebTypes | null> {
  try {
    if (!existsSync(path)) {
      return null;
    }
    const content = await readFile(path, 'utf-8');
    return JSON.parse(content) as WebTypes;
  } catch (error) {
    logger.warn(`无法加载现有 web-types: ${error}`);
    return null;
  }
}

async function generate(options: GenerateOptions): Promise<void> {
  const startTime = Date.now();
  const wxOutput = options.wxOutput ?? resolve(dirname(options.output), WX_OUTPUT_FILENAME);

  logger.info('🚀 开始生成 web-types...');
  logger.info(`   MPX 输出路径: ${options.output}`);
  logger.info(`   wx 输出路径: ${wxOutput}`);

  if (options.noCache) {
    logger.info('   清除缓存...');
    await clearCache();
  }

  // 1. 提取微信组件数据
  logger.info('📥 提取微信小程序组件数据...');
  const extractor = new WechatDocsExtractor();
  const components = await extractor.extract();
  logger.success(`   提取了 ${components.length} 个组件`);

  // 2. 转换组件数据
  logger.info('🔄 转换组件数据...');
  const componentTransformer = new ComponentTransformer();
  const componentElements = [
    ...componentTransformer.transformAll(components),
    ...getWechatContextualElements()
  ];
  const wxElements = [
    ...componentElements,
    ...getWechatNativeElements()
  ];
  const wxElementNames = new Set(wxElements.map(element => element.name));
  logger.success(`   转换了 ${wxElements.length} 个 wx HTML 元素`);

  // 3. 转换指令数据
  logger.info('🔄 转换指令数据...');
  const mpxDirectives = getMpxDirectivesConfig();
  const directiveTransformer = new DirectiveTransformer(mpxDirectives);
  const wxDirectives = directiveTransformer.transformWxDirectives();
  const globalAttributes = directiveTransformer.transformGlobalAttributes();
  const topLevelElements = getMpxTopLevelElements();
  logger.success(`   转换了 ${wxDirectives.length} 个 wx: 指令`);
  logger.success(`   转换了 ${globalAttributes.length} 个 MPX 全局属性模式`);

  // 4. 加载现有 web-types（保留自定义内容）
  logger.info('📄 加载现有 web-types...');
  const existingMpxWebTypes = await loadExistingWebTypes(options.output);
  const existingWxWebTypes = await loadExistingWebTypes(wxOutput);
  if (existingMpxWebTypes) {
    logger.success('   已加载现有 MPX 配置');
  } else {
    logger.warn('   未找到现有 MPX 配置，将创建新文件');
  }
  assertNoComponentMetadataRegression(existingWxWebTypes ?? existingMpxWebTypes, components);

  // 5. 构建最终的 MPX 与微信原生 web-types
  logger.info('🔨 构建 web-types...');
  const mpxBuilder = new WebTypesBuilder({ version: '3.4.0' });

  mpxBuilder
    .addAttributes(globalAttributes)
    .addWxDirectives(wxDirectives)
    .addTopLevelElements(topLevelElements);

  // 合并现有内容（保留自定义的 vue-components 等）
  if (existingMpxWebTypes) {
    mpxBuilder.mergeExisting(existingMpxWebTypes, {
      excludeElementNames: wxElementNames
    });
  }

  mpxBuilder.addElements(getMpxNativeElements());

  const mpxWebTypes = mpxBuilder.build();
  const wxWebTypes = new WebTypesBuilder({
    version: '0.0.0',
    framework: undefined,
    name: 'wx',
    defaultIcon: 'icons/wx.svg',
    requiredContext: { kind: 'framework', name: 'mpx' }
  })
    .addElements(wxElements)
    .build();

  // 统计输出内容
  const mpxStats = {
    elements: mpxWebTypes.contributions.html.elements?.length || 0,
    attributes: mpxWebTypes.contributions.html.attributes?.length || 0,
    directives: mpxWebTypes.contributions.html['wx-directives']?.length || 0,
    components: mpxWebTypes.contributions.html['vue-components']?.length || 0,
    topLevelElements: mpxWebTypes.contributions.html['vue-file-top-elements']?.length || 0
  };
  const wxElementCount = wxWebTypes.contributions.html.elements?.length || 0;

  logger.info('📊 生成统计:');
  logger.info(`   - MPX HTML 元素: ${mpxStats.elements}`);
  logger.info(`   - MPX HTML 属性: ${mpxStats.attributes}`);
  logger.info(`   - MPX wx: 指令: ${mpxStats.directives}`);
  logger.info(`   - MPX Vue 组件: ${mpxStats.components}`);
  logger.info(`   - MPX 顶层块: ${mpxStats.topLevelElements}`);
  logger.info(`   - wx HTML 元素: ${wxElementCount}`);

  // 6. 输出结果
  if (options.dryRun) {
    logger.warn('🔍 预览模式 - 不写入文件');
    if (options.verbose) {
      console.log(JSON.stringify({ mpx: mpxWebTypes, wx: wxWebTypes }, null, 2));
    }
  } else {
    for (const outputPath of [options.output, wxOutput]) {
      const outputDir = dirname(outputPath);
      if (!existsSync(outputDir)) {
        await mkdir(outputDir, { recursive: true });
      }
    }

    await writeFile(options.output, JSON.stringify(mpxWebTypes, null, 2), 'utf-8');
    await writeFile(wxOutput, JSON.stringify(wxWebTypes, null, 2), 'utf-8');
    logger.success(`✅ 已写入: ${options.output}`);
    logger.success(`✅ 已写入: ${wxOutput}`);
  }

  const duration = ((Date.now() - startTime) / 1000).toFixed(2);
  logger.success(`\n🎉 完成! 耗时 ${duration}s`);
}

// CLI 定义
const program = new Command();

program
  .name('web-types-generator')
  .description('从微信小程序文档生成 web-types.json')
  .version('1.0.0');

program
  .command('generate')
  .description('生成 web-types.json 文件')
  .option('-d, --dry-run', '预览模式，不写入文件', false)
  .option('-n, --no-cache', '禁用缓存，重新获取所有数据', false)
  .option('-o, --output <path>', '输出文件路径', DEFAULT_OUTPUT_PATH)
  .option('--wx-output <path>', 'wx 输出文件路径（默认与 MPX 输出文件同目录）')
  .option('-v, --verbose', '显示详细输出', false)
  .action(async (options: GenerateOptions) => {
    try {
      await generate(options);
    } catch (error) {
      logger.error(`生成失败: ${error}`);
      process.exit(1);
    }
  });

program
  .command('clear-cache')
  .description('清除 HTTP 缓存')
  .action(async () => {
    try {
      await clearCache();
      logger.success('缓存已清除');
    } catch (error) {
      logger.error(`清除缓存失败: ${error}`);
      process.exit(1);
    }
  });

// 默认命令
program
  .action(async () => {
    try {
      await generate({
        dryRun: false,
        noCache: false,
        output: DEFAULT_OUTPUT_PATH,
        wxOutput: undefined,
        verbose: false
      });
    } catch (error) {
      logger.error(`生成失败: ${error}`);
      process.exit(1);
    }
  });

program.parse();
