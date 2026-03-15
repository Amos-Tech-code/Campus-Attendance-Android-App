package com.amos_tech_code.smartattend.data.local

interface SessionProvider {

    fun getValidToken(): String?

    fun saveFcmToken(token: String)
    
}
