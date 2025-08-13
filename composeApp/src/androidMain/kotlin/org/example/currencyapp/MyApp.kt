package org.example.currencyapp

import android.app.Application
import org.example.currencyapp.di.initializeKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
    }
}
