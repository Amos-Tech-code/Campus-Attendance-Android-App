package com.amos_tech_code.smartattend.data.network

import com.amos_tech_code.smartattend.data.network.utils.LiveAttendanceUpdate
import com.amos_tech_code.smartattend.di.BASE_URL
import com.amos_tech_code.smartattend.domain.response.AttendanceMarkedEventDto
import com.amos_tech_code.smartattend.domain.response.LiveAttendanceSnapshot
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class LiveAttendanceSseClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    fun connect(sessionId: String): Flow<LiveAttendanceUpdate> = callbackFlow {

        val request = Request.Builder()
            .url("${BASE_URL}attendance/$sessionId/live")
            .addHeader("Accept", "text/event-stream")
            .build()

        val call = okHttpClient.newCall(request)

        val job = launch(Dispatchers.IO) {
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    close(IllegalStateException("SSE failed: ${response.code}"))
                    return@launch
                }

                val source = response.body?.source()
                    ?: run {
                        close(IllegalStateException("Empty SSE body"))
                        return@launch
                    }

                while (!source.exhausted() && isActive) {
                    val line = source.readUtf8Line() ?: continue

                    if (line.startsWith("event:")) {
                        val eventType = line.removePrefix("event:").trim()
                        val dataLine = source.readUtf8Line()
                            ?.removePrefix("data:")
                            ?.trim()

                        if (dataLine != null) {
                            when (eventType) {
                                "INITIAL_STATE" -> {
                                    val snapshot = gson.fromJson(
                                        dataLine,
                                        LiveAttendanceSnapshot::class.java
                                    )
                                    trySend(
                                        LiveAttendanceUpdate.InitialState(snapshot)
                                    )
                                }

                                "ATTENDANCE_MARKED" -> {
                                    val event = gson.fromJson(
                                        dataLine,
                                        AttendanceMarkedEventDto::class.java
                                    )
                                    trySend(
                                        LiveAttendanceUpdate.AttendanceMarked(event)
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose {
            job.cancel()
            call.cancel()
        }
    }
}
