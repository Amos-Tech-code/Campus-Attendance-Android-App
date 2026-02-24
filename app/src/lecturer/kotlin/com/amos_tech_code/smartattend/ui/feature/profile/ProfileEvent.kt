package com.amos_tech_code.smartattend.ui.feature.profile

// ProfileEvent.kt
sealed class ProfileEvent {
    data class ShowErrorMessage(val message: String) : ProfileEvent()
    data class ShowSuccessMessage(val message: String) : ProfileEvent()
    data object NavigateToInstitutionSetUp : ProfileEvent()
    data class NavigateToInstitutionDetail(val institutionId: String) : ProfileEvent()
    data object NavigateToNotifications : ProfileEvent()
    data object NavigateToDataManagement : ProfileEvent()
    data object NavigateToPreferences : ProfileEvent()
    data object ExportData : ProfileEvent()
    data object LogOut : ProfileEvent()
}


sealed class ProfileUiEvent {
    data object RefreshData : ProfileUiEvent()
    data object ToggleAddInstitution : ProfileUiEvent()
    data class SelectInstitution(val institutionId: String) : ProfileUiEvent()
    data object EditProfile : ProfileUiEvent()
    data object ExportProfileData : ProfileUiEvent()
    data object ManageNotifications : ProfileUiEvent()
    data object ManageData : ProfileUiEvent()
    data object ManagePreferences : ProfileUiEvent()
    data object ViewAppInfo : ProfileUiEvent()
    data object HideEditNameSheet : ProfileUiEvent()
    data class OnEditingNameChanged(val name: String) : ProfileUiEvent()
    data object SaveEditedName : ProfileUiEvent()
    data object ClearBottomSheetError : ProfileUiEvent()
    data object LogOut : ProfileUiEvent()
}