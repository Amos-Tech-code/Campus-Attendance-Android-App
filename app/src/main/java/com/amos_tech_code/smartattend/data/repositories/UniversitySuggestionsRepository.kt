package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.ProgrammeSuggestionRequest
import com.amos_tech_code.smartattend.domain.request.UniversitySuggestionRequest
import com.amos_tech_code.smartattend.domain.response.ProgrammeSuggestion
import com.amos_tech_code.smartattend.domain.response.UniversitySuggestion

interface UniversitySuggestionsRepository {

    suspend fun fetchMatchingUniversities(request: UniversitySuggestionRequest): ApiResult<List<UniversitySuggestion>>

    suspend fun fetchMatchingProgrammes(request: ProgrammeSuggestionRequest): ApiResult<List<ProgrammeSuggestion>>

}