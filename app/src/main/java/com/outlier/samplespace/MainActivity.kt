package com.outlier.samplespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.outlier.samplespace.ui.OutlierApp
import com.outlier.samplespace.ui.theme.OutlierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OutlierTheme {
                OutlierApp()
            }
        }
    }
}
