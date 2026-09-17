package com.wasama.hustlehub.data.remote

import retrofit2.http.GET

data class ExchangeRateResponse(val base: String, val rates: Map<String, Double>, val date: String)
data class ZenQuote(val q: String, val a: String)

interface ExchangeRateApi {
    @GET("v4/latest/USD")
    suspend fun getUsdRates(): ExchangeRateResponse

    @GET("v4/latest/ZAR")
    suspend fun getZarRates(): ExchangeRateResponse
}

interface ZenQuotesApi {
    @GET("api/random")
    suspend fun getRandom(): List<ZenQuote>
}

object ApiConstants {
    const val EXCHANGE_BASE = "https://api.exchangerate-api.com/"
    const val ZEN_BASE = "https://zenquotes.io/"
    fun usdToZar(usd: Double, zarPerUsd: Double): Double = usd * zarPerUsd
    fun xpForAction(action: String, isOnTime: Boolean = false): Int = when(action) {
        "TASK" -> if (isOnTime) 60 else 10
        "INVOICE_PAID" -> 100
        else -> 0
    }
    fun levelFromXp(xp: Int): Int = (xp / 300) + 1
}