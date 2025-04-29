package com.example.budgetpiggy.utils

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object CurrencyManager {
    private const val PREFS        = "currency_prefs"
    private const val KEY_BASE     = "rates_base"
    private const val KEY_RATES    = "rates_json"
    private const val API_ENDPOINT = "https://api.exchangerate-api.com/v4/latest/"


    suspend fun getRateMap(context: Context, baseCurrency: String): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            if (NetworkUtils.isInternetAvailable(context)) {
                try {
                    val json = fetchFromApi(baseCurrency)
                    saveLocally(context, baseCurrency, json)
                    parseRates(json)
                } catch (_: Exception) {

                    loadFromPrefs(context, baseCurrency)
                }
            } else {
                loadFromPrefs(context, baseCurrency)
            }
        }
    }

    private fun fetchFromApi(base: String): String {
        val conn = URL("$API_ENDPOINT$base").openConnection().apply { connect() }
        return BufferedReader(InputStreamReader(conn.getInputStream())).use { it.readText() }
    }

    private fun saveLocally(context: Context, base: String, json: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_BASE, base)
            putString(KEY_RATES, json)
        }
    }

    private fun loadFromPrefs(context: Context, base: String): Map<String, Double> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedBase = prefs.getString(KEY_BASE, null)
        val json       = prefs.getString(KEY_RATES, null)
        return if (savedBase == base && json != null) {
            parseRates(json)
        } else {

            mapOf(base to 1.0)
        }
    }

    private fun parseRates(json: String): Map<String, Double> {
        val ratesObj = JSONObject(json).getJSONObject("rates")
        return ratesObj.keys()
            .asSequence()
            .associateWith { ratesObj.getDouble(it) }
    }
}
