package com.amos_tech_code.smartattend.utils


// Custom Exception Class
class LocationServiceException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)