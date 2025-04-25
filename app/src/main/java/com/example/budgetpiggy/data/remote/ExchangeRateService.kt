package com.example.budgetpiggy.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateService {
    @GET("v6/{apiKey}/latest/{base}")
    suspend fun latest(
        @Path("apiKey") apiKey: String,
        @Path("base")   base: String
    ): LatestResponse

    @GET("v6/{apiKey}/pair/{from}/{to}/{amount}")
    suspend fun pair(
        @Path("apiKey") apiKey: String,
        @Path("from")   from: String,
        @Path("to")     to: String,
        @Path("amount") amount: Double
    ): PairResponse
}
