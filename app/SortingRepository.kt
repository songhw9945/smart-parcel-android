package com.example.sortingsystem.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 네트워크 호출 결과를 표현하는 클래스
 *
 *   - Success: 데이터가 정상으로 들어옴
 *   - Failure: 네트워크 오류, 서버 오류 등 (message 에 이유)
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val message: String) : ApiResult<Nothing>
}

/**
 * 서버 통신을 담당하는 저장소(Repository) — 읽기 전용
 *
 * ⚠️ 보안/안전 정책: 이 앱은 장비를 제어하지 않습니다.
 *    명령 전송 함수(sendConveyorCommand)는 제거했고, 조회 함수만 남깁니다.
 *
 * 모든 호출은 withContext(Dispatchers.IO) 로 백그라운드에서 실행됩니다.
 */
class SortingRepository(
    private val api: SortingApi = RetrofitClient.api
) {

    suspend fun fetchStatus(): ApiResult<DeviceStatusDto> =
        safeCall { api.getStatus() }

    suspend fun fetchSortLogs(): ApiResult<List<SortingLogDto>> =
        safeCall { api.getSortLogs() }

    suspend fun fetchErrorLogs(): ApiResult<List<ErrorLogDto>> =
        safeCall { api.getErrorLogs() }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.logs)
                is ApiResult.Failure -> result
            }
        }

    suspend fun fetchShippingLogs(): ApiResult<List<ShippingLogDto>> =
        safeCall { api.getShippingLogs() }

    suspend fun fetchBlackboxEvents(): ApiResult<List<BlackboxEventDto>> =
        safeCall { api.getBlackboxEvents() }

    suspend fun fetchCarStatus(): ApiResult<List<CarStatusDto>> =
        safeCall { api.getCarStatus() }

    suspend fun fetchEvents(limit: Int = 20): ApiResult<List<EventDto>> =
        safeCall { api.getEvents(limit) }

    /**
     * Retrofit 호출을 안전하게 감싸는 공통 함수.
     */
    private suspend fun <T> safeCall(
        block: suspend () -> retrofit2.Response<T>
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Success(Unit as T)
                }
            } else {
                ApiResult.Failure("서버 오류 (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "알 수 없는 네트워크 오류")
        }
    }
}
