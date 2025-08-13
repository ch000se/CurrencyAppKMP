package org.example.currencyapp.data.remote.api

import ApiResponse
import Currency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.currencyapp.domain.CurrencyApiService
import org.example.currencyapp.domain.PreferencesRepository
import org.example.currencyapp.domain.model.CurrencyCode
import org.example.currencyapp.domain.model.RequestState
import toCurrency

class CurrencyApiServiceImpl(
    private val preferences: PreferencesRepository
) : CurrencyApiService {
    companion object {
        const val ENDPOINT = "https://api.currencyapi.com/v3/latest"
        const val API_KEY = "cur_live_37FnH43zh93WzPJNFKQK5G1VSKtbfKp7ovlvjwfH"
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json{
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout){
            requestTimeoutMillis = 15000
        }

        install(DefaultRequest){
            headers{
                append("apikey", API_KEY)
            }
        }
    }


    override suspend fun getLatestExchangeRates(): RequestState<List<Currency>> {
        return try {
            val response: HttpResponse = httpClient.get(ENDPOINT)
            if(response.status.value == 200) {
                val apiResponse = Json.decodeFromString<ApiResponse>(response.body())

                val availableCurrencyCodes = apiResponse.data.keys
                    .filter{
                        CurrencyCode.entries
                            .map{code -> code.name}
                            .toSet()
                            .contains(it)
                    }

                val availableCurrency = apiResponse.data.values
                    .filter { currency ->
                        availableCurrencyCodes.contains(currency.code)
                    }
                    .map { currency ->
                        currency.toCurrency()
                    }

                val lastUpdated = apiResponse.meta.lastUpdatedAt
                preferences.saveLastUpdatedAt(lastUpdated)

                RequestState.Success(data = availableCurrency)
            } else {
                RequestState.Error("Failed to fetch data: ${response.status.description}")
            }
        } catch (e: Exception) {
            RequestState.Error(e.message ?: "Unknown error occurred")
        }

    }
}