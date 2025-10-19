package com.amos_tech_code.smartattend.data.network.utils

import com.amos_tech_code.smartattend.data.local.shared_prefs.SmartAttendSession
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val session: SmartAttendSession
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.getValidToken()
        val newRequest = chain.request().newBuilder()
            .apply {
                if (!token.isNullOrEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()
        return chain.proceed(newRequest)
    }
}