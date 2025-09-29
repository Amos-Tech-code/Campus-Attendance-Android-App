package com.amos_tech_code.smartattend

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.amos_tech_code.smartattend.ui.feature.register.RegisterScreen
import com.amos_tech_code.smartattend.ui.theme.SmartAttendTheme

class MainActivity : BaseSmartAttendActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        setContent {
            SmartAttendTheme(darkTheme = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    App(
                        navController = rememberNavController()
                    )
                }
            }
        }
    }

}