package com.amos_tech_code.smartattend.ui.feature.home

sealed class HomeState {
    data object Nothing : HomeState()
    data object Loading : HomeState()
    data object Success : HomeState()
    data class Error(val message: String) : HomeState()
}