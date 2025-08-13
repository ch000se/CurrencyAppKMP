package org.example.currencyapp.data.local

import Currency
import androidx.compose.ui.Modifier
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.delete
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.example.currencyapp.domain.MongoRepository
import org.example.currencyapp.domain.model.RequestState

class MongoImpl : MongoRepository {
    private var realm: Realm? = null

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (realm == null || realm?.isClosed() == true) {
            val config = RealmConfiguration.Builder(
                schema = setOf(Currency::class)
            )
                .compactOnLaunch()
                .build()
            realm = Realm.open(config)
        }
    }

    override suspend fun insertCurrencyData(currency: Currency) {
        realm?.writeBlocking {
            copyToRealm(currency)
        } ?: throw IllegalStateException("Realm is not configured or is closed")
    }

    override fun readCurrencyData(): Flow<RequestState<List<Currency>>> {
        return realm?.query<Currency>()
            ?.asFlow()
            ?.map { results ->
                RequestState.Success(results.list)
            } ?: flowOf(RequestState.Error("Realm is not configured or is closed"))
    }

    override fun cleanUp() {
        realm?.writeBlocking {
            val allCurrencies = this.query<Currency>()
            delete(allCurrencies)
        }
    }
}