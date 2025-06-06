package com.example.budgetpiggy.data.remote

import com.squareup.moshi.Json

data class PairResponse(
    val result: String,
    val documentation: String,
    @Json(name="time_last_update_unix")
    val timeLastUpdateUnix: Long,
    @Json(name="time_last_update_utc")
    val timeLastUpdateUtc: String,
    @Json(name="time_next_update_unix")
    val timeNextUpdateUnix: Long,
    @Json(name="time_next_update_utc")
    val timeNextUpdateUtc: String,
    @Json(name="base_code")
    val baseCode: String,
    @Json(name="target_code")
    val targetCode: String,
    @Json(name="conversion_rate")
    val conversionRate: Double,
    @Json(name="conversion_result")
    val conversionResult: Double
)
