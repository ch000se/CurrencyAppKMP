package org.example.currencyapp.presentation.screen

import Currency
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.example.currencyapp.domain.CurrencyApiService
import org.example.currencyapp.domain.MongoRepository
import org.example.currencyapp.domain.PreferencesRepository
import org.example.currencyapp.domain.model.RateStatus
import org.example.currencyapp.domain.model.RequestState

sealed class HomeUiEvent {
    data object RefreshRates : HomeUiEvent()
    data object SwitchCurrencies : HomeUiEvent()
    data class SelectSourceCurrency(val currencyCode: String) : HomeUiEvent()
    data class SelectTargetCurrency(val currencyCode: String) : HomeUiEvent()
}


class HomeViewModel(
    private val preferences: PreferencesRepository,
    private val api: CurrencyApiService,
    private val mongoDb: MongoRepository
) : ScreenModel {

    private val refreshMutex = Mutex()

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing

    private var _rateStatus: MutableState<RateStatus> = mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    private var _allCurrencies = mutableStateListOf<Currency>()
    val allCurrencies: List<Currency> = _allCurrencies

    private var _sourceCurrency: MutableState<RequestState<Currency>> =
        mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<Currency>> = _sourceCurrency
    private var _targetCurrency: MutableState<RequestState<Currency>> =
        mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<Currency>> = _targetCurrency

    init {
        screenModelScope.launch(Dispatchers.IO) {
            fetchNewRates()
            readSourceCurrency()
            readTargetCurrency()
        }
    }

    private fun readSourceCurrency() {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.readSourceCurrencyCode().collectLatest { currencyCode ->
                val selected = _allCurrencies.find { it.code == currencyCode.name }
                withContext(Dispatchers.Main) {
                    _sourceCurrency.value = selected?.let { RequestState.Success(it) }
                        ?: RequestState.Error("Source currency not found")
                }
            }
        }
    }

    private fun readTargetCurrency() {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.readTargetCurrencyCode().collectLatest { currencyCode ->
                val selected = _allCurrencies.find { it.code == currencyCode.name }
                withContext(Dispatchers.Main) {
                    _targetCurrency.value = selected?.let { RequestState.Success(it) }
                        ?: RequestState.Error("Target currency not found")
                }
            }
        }
    }


    private suspend fun fetchNewRates() = withContext(Dispatchers.IO) {
        if (!refreshMutex.tryLock()) return@withContext
        try {
            withContext(Dispatchers.Main) { _isRefreshing.value = true }

            val cacheResult = mongoDb.readCurrencyData().first()

            if (cacheResult.isSuccess()) {
                val cached = cacheResult.getSuccessData()
                if (cached.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _allCurrencies.clear()
                        _allCurrencies.addAll(cached)
                    }
                    val now = Clock.System.now().toEpochMilliseconds()
                    val fresh = preferences.isDataFresh(now)
                    if (!fresh) {
                        println("HomeViewModel: DATA NOT FRESH → fetch network")
                        cacheTheData()
                    } else {
                        println("HomeViewModel: DATA IS FRESH")
                    }
                } else {
                    println("HomeViewModel: DATABASE EMPTY → fetch network")
                    cacheTheData()
                }
            } else if (cacheResult.isError()) {
                println("HomeViewModel: ERROR READING DB ${cacheResult.getErrorMessage()} → fetch network")
                cacheTheData()
            }

            getRateStatus()
        } catch (e: Exception) {
            println("HomeViewModel: fetchNewRates exception: ${e.message} → mark stale")
            withContext(Dispatchers.Main) { _rateStatus.value = RateStatus.Stale }
        } finally {
            withContext(Dispatchers.Main) { _isRefreshing.value = false }
            refreshMutex.unlock()
        }
    }



    private suspend fun cacheTheData() {
        val fetched = api.getLatestExchangeRates()
        if (fetched.isSuccess()) {
            mongoDb.cleanUp()
            val data = fetched.getSuccessData()
            data.forEach { mongoDb.insertCurrencyData(it) }

            withContext(Dispatchers.Main) {
                _allCurrencies.clear()
                _allCurrencies.addAll(data)
                _rateStatus.value = RateStatus.Fresh
            }

            preferences.saveLastUpdatedAt(Clock.System.now().toString())
        } else if (fetched.isError()) {
            withContext(Dispatchers.Main) { _rateStatus.value = RateStatus.Stale }
        }
    }

    fun sendEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.RefreshRates -> screenModelScope.launch(Dispatchers.IO) { fetchNewRates() }
            HomeUiEvent.SwitchCurrencies -> switchCurrencies()
            is HomeUiEvent.SelectSourceCurrency -> selectSourceCurrency(event.currencyCode)
            is HomeUiEvent.SelectTargetCurrency -> selectTargetCurrency(event.currencyCode)
        }
    }

    private fun selectTargetCurrency(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveTargetCurrencyCode(code)
        }
    }

    private fun selectSourceCurrency(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveSourceCurrencyCode(code)
        }

    }

    private fun switchCurrencies() {
        val oldSource = _sourceCurrency.value
        val oldTarget = _targetCurrency.value

        _sourceCurrency.value = oldTarget
        _targetCurrency.value = oldSource

        screenModelScope.launch(Dispatchers.IO) {
            val newSource = (oldTarget as? RequestState.Success)?.data?.code
            val newTarget = (oldSource as? RequestState.Success)?.data?.code
            if (newSource != null) preferences.saveSourceCurrencyCode(newSource)
            if (newTarget != null) preferences.saveTargetCurrencyCode(newTarget)
        }
    }


    private suspend fun getRateStatus() {
        val fresh = preferences.isDataFresh(Clock.System.now().toEpochMilliseconds())
        withContext(Dispatchers.Main) {
            _rateStatus.value = if (fresh) RateStatus.Fresh else RateStatus.Stale
        }
    }

}
