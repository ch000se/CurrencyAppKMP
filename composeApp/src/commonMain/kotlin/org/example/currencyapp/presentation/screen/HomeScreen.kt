package org.example.currencyapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import org.example.currencyapp.domain.model.CurrencyType
import org.example.currencyapp.presentation.component.CurrencyPickerDialog
import org.example.currencyapp.presentation.component.HomeBody
import org.example.currencyapp.presentation.component.HomeHeader
import surfaceColor

class HomeScreen: Screen {
    @Composable
    override fun Content() {

        val viewModel = getScreenModel<HomeViewModel>()
        val rateStatus by viewModel.rateStatus
        val sourceCurrency by viewModel.sourceCurrency
        val targetCurrency by viewModel.targetCurrency
        val allCurrencies = viewModel.allCurrencies

        var amount by rememberSaveable{
            mutableStateOf(0.0)
        }

        var selectedCurrencyType: CurrencyType by remember {
            mutableStateOf(CurrencyType.None)
        }
        var dialogOpened by rememberSaveable {
            mutableStateOf(false)
        }

        if(dialogOpened && selectedCurrencyType != CurrencyType.None) {
            CurrencyPickerDialog(
                currencies = allCurrencies,
                currencyType = selectedCurrencyType,
                onConfirmClick = { currencyCode ->
                    when (selectedCurrencyType) {
                        is CurrencyType.Source -> viewModel.sendEvent(HomeUiEvent.SelectSourceCurrency(currencyCode.name))
                        is CurrencyType.Target -> viewModel.sendEvent(HomeUiEvent.SelectTargetCurrency(currencyCode.name))
                        else -> {}
                    }
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                },
                onDismiss = {
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                }
            )
        }


       Column(
           modifier = Modifier
               .fillMaxSize()
               .background(surfaceColor)
       ) {
           HomeHeader(
                status = rateStatus,
                onRatesRefresh = {
                    viewModel.sendEvent(HomeUiEvent.RefreshRates)
                },
                source = sourceCurrency,
                target = targetCurrency,
                onSwitchClick = {
                    viewModel.sendEvent(HomeUiEvent.SwitchCurrencies)
                },
                amount = amount,
                onAmountChange = {
                    amount = it
                },
                onCurrencyTypeSelected = { currencyType ->
                    selectedCurrencyType = currencyType
                    dialogOpened = true
                }
           )
           HomeBody(
                source = sourceCurrency,
                target = targetCurrency,
                amount = amount
           )
       }
    }
}