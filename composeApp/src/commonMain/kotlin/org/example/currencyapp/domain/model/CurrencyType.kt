package org.example.currencyapp.domain.model

sealed class CurrencyType(val code: CurrencyCode) {
    data class Source(val sourceCode: CurrencyCode) : CurrencyType(sourceCode)
    data class Target(val targetCode: CurrencyCode) : CurrencyType(targetCode)
    data object None : CurrencyType(CurrencyCode.USD)

}