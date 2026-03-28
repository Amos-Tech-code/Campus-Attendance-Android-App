package com.amos_tech_code.smartattend.di

import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.LiveAttendanceSseClient
import com.amos_tech_code.smartattend.data.network.utils.AuthInterceptor
import com.amos_tech_code.smartattend.data.network.utils.ConnectivityObserver
import com.amos_tech_code.smartattend.data.network.utils.ConnectivityObserverImpl
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//const val BASE_URL = "https://smart-attendance-nsjb.onrender.com/api/v1/"
const val BASE_URL = "http://10.0.2.2:8443/api/v1/"

val networkModule = module {

    // Single instance of Gson with proper configuration
    single {
        GsonBuilder()
            .serializeNulls() // This is the key! Makes Gson serialize null values
            .create()
    }

    // Single instance of OkHttpClient
    single {
        val isDebug: Boolean = getProperty("isDebug")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(get<AuthInterceptor>()) // Inject AuthInterceptor
            .build()
    }

    // Auth Interceptor
    single { AuthInterceptor(get()) }

    // Single instance of ApiService
    single<ApiService> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get()) // Use OkHttpClient from Koin
            .addConverterFactory(
                GsonConverterFactory.create(get())
            )
            .build()
            .create(ApiService::class.java)
    }

    // Single instance of Connectivity Observer
    single<ConnectivityObserver> { ConnectivityObserverImpl(get()) }

    single { LiveAttendanceSseClient(get(), get()) }


}