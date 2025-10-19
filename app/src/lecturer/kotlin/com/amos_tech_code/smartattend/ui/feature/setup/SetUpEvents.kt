package com.amos_tech_code.smartattend.ui.feature.setup

sealed class SetUpEvents {

    data class ShowErrorMessage(val message: String) : SetUpEvents()

    data object SetupComplete : SetUpEvents()

}