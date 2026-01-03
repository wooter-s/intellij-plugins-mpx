#!/usr/bin/env node
import { Command } from 'commander';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { WechatDocsExtractor } from './extractors/index.js';
import { ComponentTransformer, DirectiveTransformer } from './transformers/index.js';
import { WebTypesBuilder } from './builders/index.js';
import { getMpxDirectivesConfig } from './data/index.js';
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

interface GenerateOptions {
  dryRun: boolean;
  noCache: boolean;
  output: string;
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

  logger.info('🚀 开始生成 web-types...');
  logger.info(`   输出路径: ${options.output}`);

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
  const htmlElements = componentTransformer.transformAll(components);
  logger.success(`   转换了 ${htmlElements.length} 个 HTML 元素`);

  // 3. 转换指令数据
  logger.info('🔄 转换指令数据...');
  const mpxDirectives = getMpxDirectivesConfig();
  const directiveTransformer = new DirectiveTransformer(mpxDirectives);
  const wxDirectives = directiveTransformer.transformWxDirectives();
  const bindAttributes = directiveTransformer.transformBindDirectives();
  logger.success(`   转换了 ${wxDirectives.length} 个 wx: 指令`);
  logger.success(`   转换了 ${bindAttributes.length} 个事件绑定属性`);

  // 4. 加载现有 web-types（保留自定义内容）
  logger.info('📄 加载现有 web-types...');
  const existingWebTypes = await loadExistingWebTypes(options.output);
  if (existingWebTypes) {
    logger.success('   已加载现有配置');
  } else {
    logger.warn('   未找到现有配置，将创建新文件');
  }

  // 5. 构建最终的 web-types
  logger.info('🔨 构建 web-types...');
  const builder = new WebTypesBuilder({ version: '3.4.0' });

  builder
    .addElements(htmlElements)
    .addAttributes(bindAttributes)
    .addDirectives(wxDirectives);

  // 合并现有内容（保留自定义的 vue-components 等）
  if (existingWebTypes) {
    builder.mergeExisting(existingWebTypes);
  }

  const webTypes = builder.build();

  // 统计输出内容
  const stats = {
    elements: webTypes.contributions.html.elements?.length || 0,
    attributes: webTypes.contributions.html.attributes?.length || 0,
    directives: webTypes.contributions.html['vue-directives']?.length || 0,
    components: webTypes.contributions.html['vue-components']?.length || 0
  };

  logger.info('📊 生成统计:');
  logger.info(`   - HTML 元素: ${stats.elements}`);
  logger.info(`   - HTML 属性: ${stats.attributes}`);
  logger.info(`   - wx: 指令: ${stats.directives}`);
  logger.info(`   - Vue 组件: ${stats.components}`);

  // 6. 输出结果
  if (options.dryRun) {
    logger.warn('🔍 预览模式 - 不写入文件');
    if (options.verbose) {
      console.log(JSON.stringify(webTypes, null, 2));
    }
  } else {
    // 确保目录存在
    const outputDir = dirname(options.output);
    if (!existsSync(outputDir)) {
      await mkdir(outputDir, { recursive: true });
    }

    // 写入文件
    const content = JSON.stringify(webTypes, null, 2);
    await writeFile(options.output, content, 'utf-8');
    logger.success(`✅ 已写入: ${options.output}`);
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
        verbose: false
      });
    } catch (error) {
      logger.error(`生成失败: ${error}`);
      process.exit(1);
    }
  });

program.parse();
