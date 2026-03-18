package com.amos_tech_code.smartattend.ui.feature.profile

sealed class ProfileEvent {

    object NavigateToLogin : ProfileEvent()
    object ShowEditProfile : ProfileEvent()
    object ShowEnrollmentInfo : ProfileEvent()
    object ShowEnrollmentSheet : ProfileEvent()
    object ShowYearUpdateSheet : ProfileEvent()
    object ShowDeactivateDialog: ProfileEvent()
    data class ShowMessage(val message: String): ProfileEvent()
    data class ShowError(val error: String): ProfileEvent()

}