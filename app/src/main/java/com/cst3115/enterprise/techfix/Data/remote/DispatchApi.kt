package com.cst3115.enterprise.techfix.Data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface DispatchApi {

    /**
     * Converts the request object to JSON and posts it asynchronously.
     */
    @POST("posts")
    suspend fun sendDemoUpdate(
        @Body request: DispatchUpdateRequest
    ): DispatchUpdateResponse
}