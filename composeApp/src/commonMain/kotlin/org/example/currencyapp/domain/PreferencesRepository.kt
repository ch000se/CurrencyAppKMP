package org.example.currencyapp.domain

import kotlinx.coroutines.flow.Flow
import org.example.currencyapp.domain.model.CurrencyCode

interface PreferencesRepository {
    suspend fun saveLastUpdatedAt(lastUpdatedAt: String)
    suspend fun isDataFresh(currentTimeStamp: Long): Boolean
    suspend fun saveSourceCurrencyCode(sourceCurrencyCode: String)
    suspend fun saveTargetCurrencyCode(targetCurrencyCode: String)
    fun readSourceCurrencyCode(): Flow<CurrencyCode>
    fun readTargetCurrencyCode(): Flow<CurrencyCode>
}