package org.example.currencyapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import currencyapp.composeapp.generated.resources.Res
import currencyapp.composeapp.generated.resources.compose_multiplatform
import darkScheme
import lightScheme
import org.example.currencyapp.di.initializeKoin
import org.example.currencyapp.presentation.screen.HomeScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val colors = if(!isSystemInDarkTheme()){
            lightScheme
        } else{
            darkScheme
        }

        MaterialTheme(
            colorScheme = colors
        ) {
            Navigator(HomeScreen())
        }
    }
}