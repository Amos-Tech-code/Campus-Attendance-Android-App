package com.amos_tech_code.smartattend.ui.feature.student_lookup

import com.amos_tech_code.smartattend.domain.response.StudentLookupResponse

data class StudentLookupState(
    val registrationNumber: String = "",
    val isLoading: Boolean = false,
    val studentData: StudentLookupResponse? = null,
    val error: String? = null,
    val isSearchEnabled: Boolean = false
)
