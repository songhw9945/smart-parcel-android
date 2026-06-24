package com.example.sortingsystem.data.network

import com.google.gson.annotations.SerializedName

/**
 * 서버(Flask)와 주고받는 데이터 모델 모음 (읽기 전용)
 *
 * ⚠️ 보안/안전 정책: 명령 전송용 모델(ConveyorCommand)은 제거했습니다.
 *    아래는 모두 서버 응답을 받기 위한 조회용 모델입니다.
 *
 * 서버가 보내는 JSON 필드명에 맞춰 @SerializedName 으로 매핑합니다.
 */

// ── GET /api/status : 장비 상태 ──────────────────────────────────────
data class DeviceStatusDto(
    val conveyorStatus: String = "연결전",
    val conveyorSpeed: Double = 0.0,
    val robotArmStatus: String = "대기",
    val ocrCamStatus: String = "정상",
    val qrCamStatus: String = "정상",
    val emergencyStop: Boolean = false,
    val inputUnitStatus: String = "대기",
    val todaySortedCount: Int = 0,
    val todayErrorCount: Int = 0,
    val successRate: Double = 0.0,
    val conveyorOnline: Boolean = false
)

// ── GET /api/logs/sort : 분류 성공 로그 ──────────────────────────────
data class SortingLogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName(value = "tracking_number", alternate = ["TrackingNumber"])
    val trackingNumber: String = "",
    @SerializedName(value = "recognition_type", alternate = ["RecognitionType"])
    val recognitionType: String = "",
    @SerializedName("region") val region: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName(value = "error_type", alternate = ["ErrorType"])
    val errorType: String = "",
    @SerializedName(value = "processing_time", alternate = ["ProcessingTime"])
    val processingTime: Double = 0.0,
    @SerializedName("confidence") val confidence: Double = 0.0,
    @SerializedName(value = "image_path", alternate = ["ImagePath"])
    val imagePath: String? = null
)

// ── GET /api/logs/error : 오류 로그 ──────────────────────────────────
data class ErrorLogResponse(
    @SerializedName("logs") val logs: List<ErrorLogDto> = emptyList()
)

data class ErrorLogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("package_id") val packageId: Int = 0,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("image_path") val imagePath: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ── GET /api/logs/shipping : 출고 로그 ───────────────────────────────
data class ShippingLogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName(value = "tracking_number", alternate = ["TrackingNumber"])
    val trackingNumber: String = "",
    @SerializedName("region") val region: String = "",
    @SerializedName("destination") val destination: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName(value = "slot_number", alternate = ["SlotNumber"])
    val slotNumber: Int = 0
)

// ── GET /api/blackbox/events : 블랙박스 이벤트 ───────────────────────
data class BlackboxEventDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName(value = "event_type", alternate = ["EventType"])
    val eventType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName(value = "image_path", alternate = ["ImagePath"])
    val imagePath: String = "",
    @SerializedName("severity") val severity: String = "",
    @SerializedName(value = "tracking_number", alternate = ["TrackingNumber"])
    val trackingNumber: String = ""
)

// ── GET /api/cars/status : 자동차(지게차) 상태 ───────────────────────
data class CarStatusDto(
    @SerializedName("car_id") val carId: String = "",
    @SerializedName("car_name") val carName: String = "",
    @SerializedName("status") val status: String = "출발전",
    @SerializedName("filled_slots") val filledSlots: Int = 0,
    @SerializedName("total_slots") val totalSlots: Int = 0,
    @SerializedName("last_updated") val lastUpdated: String? = null
)

// ── GET /api/events?limit=N : 최근 이벤트 이력 ───────────────────────
// /api/status 와 동일하게 카멜케이스 응답이라 @SerializedName 불필요.
// 혹시 서버가 snake_case 로 주면 image_url, package_id, event_type 에 alternate 추가.
data class EventDto(
    val id: Int = 0,
    val eventType: String = "",
    val severity: String = "",
    val message: String = "",
    val imageUrl: String? = null,
    val packageId: Int? = null,
    val timestamp: String? = null
)

// ※ ConveyorCommand (POST /api/conveyor/command 명령 전송용) 는
//    제어 기능 제거 정책에 따라 삭제되었습니다.
