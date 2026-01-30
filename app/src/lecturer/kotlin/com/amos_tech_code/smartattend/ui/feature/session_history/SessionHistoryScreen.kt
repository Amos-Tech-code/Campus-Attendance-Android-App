package com.amos_tech_code.smartattend.ui.feature.session_history

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.amos_tech_code.smartattend.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionHistoryScreen(
    navController: NavController,
    viewModel: SessionHistoryViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is SessionHistoryEvent.ShowErrorMessage -> {
                Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()


}