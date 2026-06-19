package com.firefly.controller.data.api

import com.firefly.controller.data.model.*
import retrofit2.http.*

interface FireflyApi {
    
    @GET("api/health")
    suspend fun health(): Map<String, String>

    @GET("api/status")
    suspend fun getStatus(): StatusResponse
}
