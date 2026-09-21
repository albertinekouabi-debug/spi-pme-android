package com.spipme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spipme.app.ui.navigation.SpiPmeNavGraph
import com.spipme.app.ui.theme.SpiPmeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpiPmeAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val appViewModel: AppViewModel = hiltViewModel()
                    val ecranDepart by appViewModel.ecranDepart.collectAsStateWithLifecycle()

                    val ecran = ecranDepart
                    if (ecran == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SpiPmeNavGraph(ecranDepart = ecran)
                    }
                }
            }
        }
    }
}
