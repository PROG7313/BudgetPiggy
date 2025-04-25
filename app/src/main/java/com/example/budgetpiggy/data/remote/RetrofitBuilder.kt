// File: app/src/main/java/com/example/budgetpiggy/data/remote/RetrofitBuilder.kt
package com.example.budgetpiggy.data.remote

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitBuilder {
    private const val BASE_URL = "https://v6.exchangerate-api.com/"

    val api: ExchangeRateService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder().build()
            ))
            .build()
            .create(ExchangeRateService::class.java)
    }
}
