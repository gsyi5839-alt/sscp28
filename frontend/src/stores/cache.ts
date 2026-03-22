import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Cache entry with TTL support
 */
interface CacheEntry<T = unknown> {
  data: T
  timestamp: number
  ttl: number // milliseconds
}

/**
 * Pinia cache store for API response caching
 * Provides in-memory cache with TTL expiration
 */
export const useCacheStore = defineStore('cache', () => {
  // Internal cache storage
  const entries = ref<Record<string, CacheEntry>>({})

  /**
   * Get cached value by key, returns null if expired or not found
   */
  function get<T>(key: string): T | null {
    const entry = entries.value[key]
    if (!entry) return null

    // Check if entry has expired
    if (Date.now() - entry.timestamp > entry.ttl) {
      delete entries.value[key]
      return null
    }
    return entry.data as T
  }

  /**
   * Set cache value with TTL in milliseconds
   * @param key - cache key
   * @param data - data to cache
   * @param ttlMs - time to live in milliseconds (default: 1 hour)
   */
  function set<T>(key: string, data: T, ttlMs: number = 3600000): void {
    entries.value[key] = {
      data,
      timestamp: Date.now(),
      ttl: ttlMs
    }
  }

  /**
   * Remove a specific cache entry
   */
  function remove(key: string): void {
    delete entries.value[key]
  }

  /**
   * Clear all cache entries
   */
  function clear(): void {
    entries.value = {}
  }

  return { get, set, remove, clear }
})
