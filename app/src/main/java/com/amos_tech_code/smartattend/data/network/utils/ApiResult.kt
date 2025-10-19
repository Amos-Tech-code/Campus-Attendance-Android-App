package com.amos_tech_code.smartattend.data.network.utils

import java.io.IOException

sealed class ApiResult<out T> {

    data class Success<out T>(val data: T): ApiResult<T>()

    data class Failure(val error: ApiError): ApiResult<Nothing>()
}


sealed class ApiError {

    data class HttpError(val statusCode: Int, val message: String): ApiError()

    data class NetworkError(val exception: IOException): ApiError()

    data class UnknownError(val throwable: Throwable): ApiError()

}