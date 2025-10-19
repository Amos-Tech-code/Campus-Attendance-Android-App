package com.amos_tech_code.smartattend.ui.feature.home

sealed class HomeEvent {

    object CompleteProfile : HomeEvent()

    data class ShowErrorMessage(val message: String) : HomeEvent()
}