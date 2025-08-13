package org.example.currencyapp.domain.model

import androidx.compose.ui.graphics.Color
import freshColor
import staleColor

enum class RateStatus(
    val title: String,
    val color: Color
) {
    Idle(
        title = "Idle",
        color = Color.White
    ),
    Fresh(
        title = "Fresh",
        color = freshColor
    ),
    Stale(
        title = "Rates are not fresh",
        color = staleColor
    )

}