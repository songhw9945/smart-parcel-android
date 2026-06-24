package com.example.sortingsystem.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Flask 서버 REST API 인터페이스 (읽기 전용)
 *
 * ⚠️ 보안/안전 정책: 이 앱은 장비를 제어하지 않습니다.
 *    명령 전송(POST) 엔드포인트는 모두 제거했고, 조회(GET)만 남깁니다.
 *
 * 엔드포인트:
 *   GET  api/status          → 장비 상태
 *   GET  api/logs/sort       → 분류 성공 로그 리스트
 *   GET  api/logs/error      → 오류 로그 ({"logs":[...]} 로 감싸짐)
 *   GET  api/logs/shipping   → 출고 로그 리스트
 *   GET  api/blackbox/events → 블랙박스 이벤트 리스트
 *   GET  api/cars/status     → 자동차 상태 리스트
 */
interface SortingApi {

    @GET("api/status")
    suspend fun getStatus(): Response<DeviceStatusDto>

    @GET("api/logs/sort")
    suspend fun getSortLogs(): Response<List<SortingLogDto>>

    @GET("api/logs/error")
    suspend fun getErrorLogs(): Response<ErrorLogResponse>

    @GET("api/logs/shipping")
    suspend fun getShippingLogs(): Response<List<ShippingLogDto>>

    @GET("api/blackbox/events")
    suspend fun getBlackboxEvents(): Response<List<BlackboxEventDto>>

    @GET("api/cars/status")
    suspend fun getCarStatus(): Response<List<CarStatusDto>>

    @GET("api/events")
    suspend fun getEvents(@retrofit2.http.Query("limit") limit: Int = 20): Response<List<EventDto>>
}
