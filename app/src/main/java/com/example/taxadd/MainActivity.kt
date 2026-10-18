package com.example.taxadd

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taxadd.ui.TaxAddApp
import com.example.taxadd.ui.TaxAddViewModel

class MainActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "taxadd_shared_preferences"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPreferences = applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        setContent {
            val taxAddViewModel: TaxAddViewModel = viewModel(
                factory = TaxAddViewModel.provideFactory(sharedPreferences)
            )
            TaxAddApp(viewModel = taxAddViewModel)
        }
    }
}