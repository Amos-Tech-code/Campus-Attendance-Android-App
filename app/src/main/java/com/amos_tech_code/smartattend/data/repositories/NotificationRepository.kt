package com.amos_tech_code.smartattend.data.repositories

import com.amos_tech_code.smartattend.domain.response.GenericResponse
import com.amos_tech_code.smartattend.data.network.ApiService
import com.amos_tech_code.smartattend.data.network.safeApiCall
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.domain.request.FCMTokenRequest
import com.amos_tech_code.smartattend.domain.response.NotificationCountsDto
import com.amos_tech_code.smartattend.domain.response.NotificationDto
import com.amos_tech_code.smartattend.domain.response.PaginatedNotificationsDto

class NotificationRepository(
    private val apiService: ApiService
) {

    suspend fun getUnreadNotifications(limit: Int = 50): ApiResult<List<NotificationDto>> {
        return safeApiCall {
            apiService.getUnreadNotifications(limit)
        }
    }

    suspend fun getNotificationHistory(page: Int = 0, pageSize: Int = 20): ApiResult<PaginatedNotificationsDto> {
        return safeApiCall {
            apiService.getNotificationHistory(page, pageSize)
        }
    }

    suspend fun getNotificationById(notificationId: String): ApiResult<NotificationDto> {
        return safeApiCall {
            apiService.getNotificationById(notificationId)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): ApiResult<Unit> {
        return safeApiCall {
            apiService.markNotificationAsRead(notificationId)
        }
    }

    suspend fun markAllNotificationsAsRead(): ApiResult<GenericResponse> {
        return safeApiCall {
            apiService.markAllNotificationsAsRead()
        }
    }

    suspend fun deleteNotification(notificationId: String): ApiResult<GenericResponse> {
        return safeApiCall {
            apiService.deleteNotification(notificationId)
        }
    }

    suspend fun getNotificationCounts(): ApiResult<NotificationCountsDto> {
        return safeApiCall {
            apiService.getNotificationCounts()
        }
    }

    suspend fun updateFCMToken(isFromLecturerFlavor: Boolean = true, token: String) : ApiResult<GenericResponse> {
        val tokenRequest = FCMTokenRequest(token)
        return safeApiCall {
            if (isFromLecturerFlavor) {
                apiService.updateLecturerFCMToken(tokenRequest)
            } else {
                apiService.updateStudentFCMToken(tokenRequest)
            }
        }
    }
}