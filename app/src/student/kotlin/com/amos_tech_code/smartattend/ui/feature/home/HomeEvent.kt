package com.amos_tech_code.smartattend.ui.feature.home

sealed class HomeEvent {
    data class ShowErrorMessage(val message: String) : HomeEvent()
    data class ShowSuccessMessage(val message: String) : HomeEvent()
    data object NavigateToDeviceChange : HomeEvent()
}