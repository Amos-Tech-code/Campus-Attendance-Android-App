package com.amos_tech_code.smartattend.ui.feature.profile

import com.amos_tech_code.smartattend.domain.models.University


// Profile State
data class ProfileState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val lecturer: Lecturer = Lecturer(),
    val institutions: List<University> = emptyList(),
    val activeInstitution: University? = null,
    val isSwitchingInstitution: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val showEditNameSheet: Boolean = false,
    val editingName: String = "",
    val editingNameError: String? = null,
    val bottomSheetErrorMessage: String? = null,
    val isExporting: Boolean = false,
    val lastSyncTime: String? = null
)

data class Lecturer(
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val profileImage: String? = null,
    val staffId: String? = null,
    val department: String? = null,
    val joinDate: String? = null
)
