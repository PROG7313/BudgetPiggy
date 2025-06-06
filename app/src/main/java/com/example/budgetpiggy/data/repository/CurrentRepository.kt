package com.example.budgetpiggy.data

import com.example.budgetpiggy.data.dao.RateDao
import com.example.budgetpiggy.data.dao.UserDao
import com.example.budgetpiggy.data.entities.RateEntity
import com.example.budgetpiggy.data.entities.UserEntity
import com.example.budgetpiggy.data.remote.ExchangeRateService

class CurrencyRepository(
    private val api: ExchangeRateService,
    private val rateDao: RateDao,
    private val userDao: UserDao,
    private val currentUserId: String
) {
    companion object {
        const val API_KEY = "261a6842e0b8059b5007b86a"
    }

    /** Read the user’s selected currency (defaults to “ZAR”) */
    suspend fun getSelectedBase(): String =
        userDao.getById(currentUserId)?.currency ?: "ZAR"

    /** Set a new base, update the user row, then re-fetch all rates */
    suspend fun setSelectedBase(newBase: String) {
        userDao.getById(currentUserId)?.let { u: UserEntity ->
            userDao.update(u.copy(currency = newBase))
        }
        loadAndStoreRates(newBase)
    }

    /** Hits `/latest/{base}`, maps the rates into Room, and inserts them */
    suspend fun loadAndStoreRates(base: String) {
        val resp = api.latest(API_KEY, base)
        val list = resp.conversionRates.map { (tgt, rate) ->
            RateEntity(
                baseCode   = resp.baseCode,
                targetCode = tgt,
                rate       = rate.toFloat()
            )
        }

        rateDao.insertAll(list)
    }

    /** Retrieve all rates we have for a given base */
    suspend fun ratesForBase(base: String): List<RateEntity> =
        rateDao.getRatesForBase(base)

    /** Retrieve a single rate */
    suspend fun getRate(base: String, target: String): Float =
        rateDao.getRate(base, target) ?: 1f
}
