package com.example.sortingsystem.data

/**
 * 시스템 전체 상태
 */
enum class SystemState(val displayName: String) {
    NORMAL("정상 가동"),
    WARNING("주의"),
    STOPPED("정지됨"),
    ERROR("오류 발생")
}

/**
 * 설비 종류
 */
enum class EquipmentType(val displayName: String) {
    QR_OCR_CAMERA("QR/OCR 캠"),
    ROBOT_ARM("myCobot Pi 로봇팔"),
    SORTING_CAMERA("분류 감시 캠"),
    CONVEYOR("컨베이어 벨트")
}

/**
 * 개별 설비 상태
 */
enum class EquipmentState(val displayName: String) {
    NORMAL("정상"),
    OPERATING("동작중"),
    RECORDING("녹화중"),
    WARNING("주의"),
    ERROR("오류"),
    OFFLINE("오프라인")
}

/**
 * 설비 정보
 */
data class Equipment(
    val type: EquipmentType,
    val state: EquipmentState
)

/**
 * 실시간 처리 현황
 */
data class ProcessingStatus(
    val inProgress: Int,      // 현재 처리 중인 박스 수
    val completedToday: Int   // 오늘 출고 완료된 박스 수
)

/**
 * 이벤트 종류
 */
enum class EventType {
    SUCCESS,    // 성공 (출고 완료 등)
    WARNING,    // 경고 (송장 인식 실패 등)
    ERROR,      // 오류 (설비 오류 등)
    INFO        // 정보 (관리자 로그인 등)
}

/**
 * 최근 이벤트
 */
data class RecentEvent(
    val id: String,
    val type: EventType,
    val title: String,
    val description: String,
    val timeAgo: String,   // 예: "2분 전"
    val imageUrl: String? = null
)
