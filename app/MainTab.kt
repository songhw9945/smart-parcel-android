package com.example.sortingsystem.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 메인 화면 하단 탭
 *
 * 구성: 홈 / 카메라 / 설정
 *  - 홈: 시스템 상태 + 최근 이벤트 모니터링
 *  - 카메라: 5대의 CCTV/감시 카메라 실시간 영상
 *  - 설정: 알림 옵션, 서버 연결 정보, 로그아웃
 */
enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("홈", Icons.Default.Home),
    CAMERA("카메라", Icons.Default.Videocam),
    SETTINGS("설정", Icons.Default.Settings)
}
