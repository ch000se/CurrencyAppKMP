package org.example.currencyapp.di

import com.russhwolf.settings.Settings
import io.ktor.http.ContentType
import org.example.currencyapp.data.local.MongoImpl
import org.example.currencyapp.data.local.PreferencesImpl
import org.example.currencyapp.data.remote.api.CurrencyApiServiceImpl
import org.example.currencyapp.domain.CurrencyApiService
import org.example.currencyapp.domain.MongoRepository
import org.example.currencyapp.domain.PreferencesRepository
import org.example.currencyapp.presentation.screen.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single<MongoRepository> { MongoImpl() }
    single<PreferencesRepository> { PreferencesImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(preferences = get()) }
    factory {
        HomeViewModel(
            preferences = get(),
            api = get(),
            mongoDb = get()
        )
    }
}

fun initializeKoin() {
    startKoin {
        modules(appModule)
    }
}