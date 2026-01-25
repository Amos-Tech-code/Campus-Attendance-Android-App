package com.amos_tech_code.smartattend.data.local

interface SessionProvider {

    fun getValidToken(): String?

    fun saveName(name: String)

    fun saveRegistrationNumber(registrationNo: String)


}
