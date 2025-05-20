// ApiService.kt
package com.aviadkorakin.search_and_dropdown

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * We don’t tell Retrofit what shape the JSON is—just read it as a List of maps.
 */
interface ApiService {
    @GET
    suspend fun search(@Url fullUrl: String): List<Map<String, Any>>
}