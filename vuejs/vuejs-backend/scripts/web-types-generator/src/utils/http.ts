import fetch from 'node-fetch';
import { getFromCache, saveToCache } from './cache.js';
import { logger } from './logger.js';

interface FetchOptions {
  useCache?: boolean;
  timeout?: number;
  retries?: number;
}

const DEFAULT_OPTIONS: FetchOptions = {
  useCache: true,
  timeout: 30000,
  retries: 3
};

async function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

export async function fetchHtml(
  url: string,
  options: FetchOptions = {}
): Promise<string> {
  const opts = { ...DEFAULT_OPTIONS, ...options };

  // Check cache first
  if (opts.useCache) {
    const cached = getFromCache(url);
    if (cached) {
      return cached;
    }
  }

  let lastError: Error | null = null;

  for (let attempt = 1; attempt <= (opts.retries ?? 3); attempt++) {
    try {
      logger.debug(`Fetching (attempt ${attempt}): ${url}`);

      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), opts.timeout);

      const response = await fetch(url, {
        signal: controller.signal,
        headers: {
          'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) WebTypesGenerator/1.0',
          'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
          'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8'
        }
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const html = await response.text();

      // Save to cache
      if (opts.useCache) {
        saveToCache(url, html);
      }

      return html;
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      logger.warn(`Fetch failed (attempt ${attempt}): ${url}`, lastError.message);

      if (attempt < (opts.retries ?? 3)) {
        await sleep(1000 * attempt); // Exponential backoff
      }
    }
  }

  throw lastError ?? new Error(`Failed to fetch: ${url}`);
}
