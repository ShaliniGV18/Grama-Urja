package com.gramaurja

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gramaurja.presentation.navigation.NavGraph
import com.gramaurja.presentation.theme.GramaUrjaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GramaUrjaTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
