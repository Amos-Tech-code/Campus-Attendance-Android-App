package com.amos_tech_code.smartattend.ui.feature.student_lookup

sealed class StudentLookupEvent {
    data class ShowError(val message: String) : StudentLookupEvent()
    data class ShowSuccess(val message: String) : StudentLookupEvent()
    object ClearData : StudentLookupEvent()
    object NavigateToDeviceApproval : StudentLookupEvent()
}
