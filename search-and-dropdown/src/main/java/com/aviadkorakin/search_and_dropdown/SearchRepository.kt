package com.aviadkorakin.search_and_dropdown

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder

/**
 * Simple in-memory cache + networking.
 * TTL (in seconds) can be updated at any time.
 */
class SearchRepository(
    initialUrl: String,
    private val apiService: ApiService,
    private var ttlSeconds: Int
) {
    private var baseUrl: String = initialUrl
    private val cache = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()

    private data class CacheEntry(
        val results: List<Map<String, Any>>,
        val timestampMs: Long
    )

    /**
     * Remove any entries older than ttlSeconds before attempting lookup.
     */
    private suspend fun pruneStale(now: Long) {
        mutex.withLock {
            cache.entries.removeAll { (_, entry) ->
                now - entry.timestampMs > ttlSeconds * 1_000L
            }
        }
    }

    /**
     * Search, using cache if entry is younger than TTL.
     */
    suspend fun search(query: String): List<Map<String, Any>> {
        val now = System.currentTimeMillis()

        // 1) remove expired entries
        pruneStale(now)

        // 2) try cache
        mutex.withLock {
            cache[query]?.let { entry ->
                Log.d("SearchRepository", "→ cache HIT for \"$query\"")
                return entry.results
            }
        }

        // 3) miss → build URL & fetch
        val encoded = URLEncoder.encode(query, "UTF-8")
        val fullUrl = if (baseUrl.contains("{query}")) {
            // Replace every occurrence of {query} with the encoded text
            baseUrl.replace("{query}", encoded)
        } else {
            // Fallback: append ?q= or &q=
            val separator = if (baseUrl.contains('?')) '&' else '?'
            "$baseUrl${separator}q=$encoded"
        }

        Log.d(
            "SearchRepository",
            "→ fetching from network: $fullUrl (TTL=${ttlSeconds}s)"
        )

        val fresh = apiService.search(fullUrl)

        // 4) store in cache
        mutex.withLock {
            cache[query] = CacheEntry(fresh, now)
        }
        return fresh
    }

    companion object {
        /**
         * Create with a dummy Retrofit base URL (required to end in ‘/’, but we override
         * with the full URL via @Url in ApiService).
         */
        fun create(initialUrl: String, ttlSeconds: Int): SearchRepository {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://dummy.invalid/") // just to satisfy Retrofit
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(ApiService::class.java)
            return SearchRepository(initialUrl, api, ttlSeconds)
        }
    }
}