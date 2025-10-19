package com.amos_tech_code.smartattend.ui.feature.student_lookup

// App Events (for navigation, toasts, etc.)
sealed class StudentLookupEvent {
    data class ShowErrorMessage(val message: String) : StudentLookupEvent()

    data class DeviceRequestUpdated(val requestId: String, val action: String) : StudentLookupEvent()
}


// UI Events (from user interactions)
sealed class StudentLookupUiEvent {
    data class SearchQueryChanged(val query: String) : StudentLookupUiEvent()
    object ClearSearch : StudentLookupUiEvent()
    object SearchStudent : StudentLookupUiEvent()
    data class ApproveDeviceRequest(val requestId: String) : StudentLookupUiEvent()
    data class RejectDeviceRequest(val requestId: String) : StudentLookupUiEvent()
}
