package com.amos_tech_code.smartattend.data.network.utils

import java.io.IOException

/**
 * Sealed class representing the result of an API call.
 * @param T The type of data expected in the success case.
 */
sealed class ApiResult<out T> {

    data class Success<out T>(val data: T): ApiResult<T>()

    data class Failure(val error: ApiError): ApiResult<Nothing>()
}


/**
 * Sealed class representing different types of errors that can occur during API calls.
 * @see ApiResult
 */
sealed class ApiError {

    data class HttpError(val statusCode: Int, val message: String): ApiError()

    data class NetworkError(val exception: IOException): ApiError()

    data class UnknownError(val throwable: Throwable): ApiError()

}


/**
 * Helper function to extract error message from ApiError
 * @return String representation of the error message
 * @see ApiError
 */

fun ApiError.extractApiErrorMessage(): String {
    return when (this) {
        is ApiError.NetworkError -> {
            "Network error: ${this.exception.message ?: "Please check your internet connection"}"
        }

        is ApiError.HttpError -> {
            "Error ${this.statusCode}: ${this.message}"
        }

        is ApiError.UnknownError -> {
            "An unexpected error occurred: ${this.throwable.message ?: "Please try again"}"
        }
    }
}