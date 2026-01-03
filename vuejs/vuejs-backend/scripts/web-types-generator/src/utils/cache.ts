import { createHash } from 'crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync, statSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import { logger } from './logger.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const CACHE_DIR = join(__dirname, '../../.cache');
const CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

interface CacheEntry {
  data: string;
  timestamp: number;
  url: string;
}

function ensureCacheDir(): void {
  if (!existsSync(CACHE_DIR)) {
    mkdirSync(CACHE_DIR, { recursive: true });
  }
}

function getCacheKey(url: string): string {
  return createHash('md5').update(url).digest('hex');
}

function getCachePath(key: string): string {
  return join(CACHE_DIR, `${key}.json`);
}

export function getFromCache(url: string): string | null {
  try {
    const key = getCacheKey(url);
    const path = getCachePath(key);

    if (!existsSync(path)) {
      return null;
    }

    const stat = statSync(path);
    const age = Date.now() - stat.mtimeMs;

    if (age > CACHE_TTL_MS) {
      logger.debug(`Cache expired for: ${url}`);
      return null;
    }

    const entry: CacheEntry = JSON.parse(readFileSync(path, 'utf-8'));
    logger.debug(`Cache hit for: ${url}`);
    return entry.data;
  } catch {
    return null;
  }
}

export function saveToCache(url: string, data: string): void {
  try {
    ensureCacheDir();
    const key = getCacheKey(url);
    const path = getCachePath(key);

    const entry: CacheEntry = {
      data,
      timestamp: Date.now(),
      url
    };

    writeFileSync(path, JSON.stringify(entry), 'utf-8');
    logger.debug(`Cached: ${url}`);
  } catch (error) {
    logger.warn(`Failed to cache: ${url}`, error);
  }
}

export async function clearCache(): Promise<void> {
  try {
    if (existsSync(CACHE_DIR)) {
      const fs = await import('fs/promises');
      await fs.rm(CACHE_DIR, { recursive: true });
      logger.info('Cache cleared');
    }
  } catch (error) {
    logger.warn('Failed to clear cache', error);
  }
}
