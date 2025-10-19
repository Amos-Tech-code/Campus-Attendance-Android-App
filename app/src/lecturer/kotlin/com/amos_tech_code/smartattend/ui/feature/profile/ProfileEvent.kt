package com.amos_tech_code.smartattend.ui.feature.profile

sealed class ProfileEvent {
    data class ShowErrorMessage(val message: String) : ProfileEvent()
    data class ShowSuccessMessage(val message: String) : ProfileEvent()
    object InstitutionUpdated : ProfileEvent()
    object NavigateToEditProfile : ProfileEvent()
}

sealed class ProfileUiEvent {
    // Institution Management
    object ToggleAddInstitution : ProfileUiEvent()
    data class NewInstitutionNameChanged(val name: String) : ProfileUiEvent()
    data class NewInstitutionDepartmentChanged(val department: String) : ProfileUiEvent()
    data class NewInstitutionCampusChanged(val campus: String) : ProfileUiEvent()
    object SaveNewInstitution : ProfileUiEvent()
    object CancelAddInstitution : ProfileUiEvent()
    data class SelectInstitution(val institutionId: String) : ProfileUiEvent()

    // Profile Actions
    object EditProfile : ProfileUiEvent()
    object RefreshData : ProfileUiEvent()
    object ExportProfileData : ProfileUiEvent()
}