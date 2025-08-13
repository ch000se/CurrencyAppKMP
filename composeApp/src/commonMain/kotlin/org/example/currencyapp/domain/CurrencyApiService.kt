package org.example.currencyapp.domain

import Currency
import org.example.currencyapp.domain.model.RequestState

interface CurrencyApiService {
    suspend fun getLatestExchangeRates(): RequestState<List<Currency>>
}