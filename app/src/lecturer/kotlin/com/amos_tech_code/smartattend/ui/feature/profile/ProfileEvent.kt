package com.amos_tech_code.smartattend.ui.feature.profile

sealed class ProfileEvent {
    data class ShowErrorMessage(val message: String) : ProfileEvent()
    data class ShowSuccessMessage(val message: String) : ProfileEvent()
    object NavigateToInstitutionSetUp : ProfileEvent()
    object ShowEditProfileSheet : ProfileEvent()
    object LogOut : ProfileEvent()
}

sealed class ProfileUiEvent {
    // Institution Management
    object ToggleAddInstitution : ProfileUiEvent()
    data class SelectInstitution(val institutionId: String) : ProfileUiEvent()

    object ShowEditNameSheet : ProfileUiEvent()
    object HideEditNameSheet : ProfileUiEvent()
    data class OnEditingNameChanged(val name: String) : ProfileUiEvent()

    object ClearBottomSheetError : ProfileUiEvent()
    object SaveEditedName : ProfileUiEvent()

    // Profile Actions
    object EditProfile : ProfileUiEvent()
    object RefreshData : ProfileUiEvent()
    object ExportProfileData : ProfileUiEvent()
}