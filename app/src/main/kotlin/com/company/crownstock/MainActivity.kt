package com.company.crownstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.company.crownstock.ui.navigation.CrownStockNavGraph
import com.company.crownstock.ui.theme.CrownStockTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Bölüm 12: "MainGraph (Bottom/Drawer Navigation kökü — uygulama açılışında
 * doğrudan bu graph yüklenir; MVP'de AuthGraph yoktur)".
 * Tek Activity mimarisi (Jetpack Compose Navigation ile).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrownStockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CrownStockNavGraph()
                }
            }
        }
    }
}
