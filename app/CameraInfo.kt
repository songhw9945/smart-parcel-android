package com.example.sortingsystem.data

/**
 * 카메라 한 대의 정보
 *
 * @param id     카메라 고유 ID (UI 에서 키로 사용)
 * @param name   화면에 보여줄 이름
 * @param url    MJPEG 스트림 URL
 */
data class CameraInfo(
    val id: String,
    val name: String,
    val url: String
)

/**
 * 시스템에서 사용하는 카메라 목록
 *
 * 모니터링 앱은 개인정보가 노출되지 않는 카메라만 표시합니다.
 *   - QR/OCR 캠, 로봇팔 캠은 송장의 개인정보가 노출되어 제외
 *   - CCTV, 블랙박스 캠은 공정 전체/물품 흐름만 보여서 표시 가능
 *
 * ⚠️ 공정 CCTV 는 현우 노트북에서 송출하며, 노트북 IP 가 바뀌면 아래 URL 도 같이 바꿔야 합니다.
 *    (현재 노트북 IP: 192.168.0.5)
 */
object CameraRegistry {
    val cameras: List<CameraInfo> = listOf(
        CameraInfo(
            id = "cctv",
            name = "공정 CCTV",
            url = "http://192.168.0.5:8083/stream/cctv"
        ),
        CameraInfo(
            id = "field",
            name = "현장 캠",
            url = "http://192.168.0.40:8082/stream/field"
        ),
        CameraInfo(
            id = "shipping",
            name = "출고 캠",
            url = "http://192.168.0.40:8082/stream/shipping"
        )
    )
}
