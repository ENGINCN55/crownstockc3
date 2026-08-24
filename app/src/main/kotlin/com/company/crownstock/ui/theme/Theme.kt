package com.company.crownstock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * NOT: Doküman bir görsel tasarım sistemi (renk paleti, tipografi) tanımlamıyor.
 * Bu yüzden burada Material3'ün standart varsayılan renk şemaları kullanıldı;
 * marka/renk kararı eklenmedi (varsayım yapılmadı).
 */
private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun CrownStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
