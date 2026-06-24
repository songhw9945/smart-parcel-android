package com.example.sortingsystem.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sortingsystem.data.Equipment
import com.example.sortingsystem.data.EquipmentState
import com.example.sortingsystem.data.EquipmentType
import com.example.sortingsystem.data.EventType
import com.example.sortingsystem.data.RecentEvent
import com.example.sortingsystem.data.SystemState
import com.example.sortingsystem.data.network.ApiResult
import com.example.sortingsystem.data.network.DeviceStatusDto
import com.example.sortingsystem.data.network.RealtimeSocketManager
import com.example.sortingsystem.data.network.SortingRepository
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 홈 화면 ViewModel (모니터링 전용)
 *
 * 설비 상태 표시 대상: 컨베이어, QR/OCR 캠 (2종)
 *   ※ 로봇팔은 상태 표시에서 제외 (WPF 측도 제외함).
 *      로봇팔의 온라인/오프라인 보고가 불안정해 모니터링 지표로 부적합하다는 판단.
 *
 * 수신 이벤트:
 *  - blackbox_event_added  : 박스 훼손 검출 → ERROR 이벤트 표시
 *  - defect_inspected      : v6.0 명세상 박스 훼손 이벤트, 불량일 때만 표시 (대비용)
 *  - emergency_stop        : 비상정지 발동 → 시스템 상태 STOPPED + 이벤트
 *  - device_connected      : 장비 연결 → 설비 상태 카드 갱신
 *  - device_disconnected   : 장비 끊김 → 설비 상태 카드 갱신
 */
class HomeViewModel(
    private val repository: SortingRepository = SortingRepository(),
    private val socketManager: RealtimeSocketManager = RealtimeSocketManager()
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val todaySorted: Int = 0,
        val todayError: Int = 0,
        val systemState: SystemState = SystemState.NORMAL,
        val equipmentList: List<Equipment> = emptyList(),
        val errorCount: Int = 0,
        val recentEvents: List<RecentEvent> = emptyList(),
        val toastMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadStatus()
        observeSocket()
        socketManager.connect()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.fetchStatus()) {
                is ApiResult.Success -> applyDeviceStatus(result.data)
                is ApiResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        toastMessage = "상태 불러오기 실패: ${result.message}"
                    )
                }
            }
        }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun observeSocket() {
        viewModelScope.launch {
            socketManager.events.collect { event ->
                handleRealtimeEvent(event.name, event.rawJson)
            }
        }
    }

    private fun applyDeviceStatus(dto: DeviceStatusDto) {
        // 로봇팔 제외 — 컨베이어 + QR/OCR 캠만 표시
        val equipments = listOf(
            Equipment(EquipmentType.QR_OCR_CAMERA, mapState(dto.qrCamStatus, dto.ocrCamStatus)),
            Equipment(EquipmentType.CONVEYOR, mapState(dto.conveyorStatus))
        )
        val errors = equipments.count { it.state == EquipmentState.ERROR || it.state == EquipmentState.OFFLINE }
        val sys = when {
            dto.emergencyStop -> SystemState.STOPPED
            errors > 0 -> SystemState.ERROR
            else -> SystemState.NORMAL
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                todaySorted = dto.todaySortedCount,
                todayError = dto.todayErrorCount,
                equipmentList = equipments,
                errorCount = errors,
                systemState = sys
            )
        }
    }

    /**
     * 서버가 주는 상태 문자열을 앱의 EquipmentState 로 변환.
     * 서버 실제 표기: conveyorStatus="연결완료", qrCamStatus/ocrCamStatus="작동중" 등.
     */
    private fun mapState(vararg statuses: String): EquipmentState {
        val priority = listOf(
            EquipmentState.ERROR,
            EquipmentState.OFFLINE,
            EquipmentState.WARNING,
            EquipmentState.OPERATING,
            EquipmentState.RECORDING,
            EquipmentState.NORMAL
        )
        val mapped = statuses.map { single ->
            when (single.trim()) {
                "오류", "ERROR", "에러", "고장" -> EquipmentState.ERROR
                "오프라인", "OFFLINE", "연결전", "연결끊김", "미연결", "끊김" -> EquipmentState.OFFLINE
                "주의", "WARNING", "경고" -> EquipmentState.WARNING
                "작동중", "동작중", "OPERATING", "운영중", "가동중", "실행중" -> EquipmentState.OPERATING
                "녹화중", "RECORDING" -> EquipmentState.RECORDING
                "정상", "NORMAL", "연결완료", "연결됨", "대기", "준비", "ONLINE", "온라인" -> EquipmentState.NORMAL
                else -> EquipmentState.NORMAL
            }
        }
        return priority.firstOrNull { it in mapped } ?: EquipmentState.NORMAL
    }

    private fun handleRealtimeEvent(name: String, json: String) {
        when (name) {
            "blackbox_event_added" -> {
                val severity = parseSummary(json, "severity")
                val eventType = parseSummary(json, "eventType") ?: "예외 발생"
                val tracking = parseSummary(json, "tracking_number")
                    ?: parseSummary(json, "invoice_no")
                    ?: ""
                val description = parseSummary(json, "description") ?: ""

                val type = if (severity == "오류") EventType.ERROR else EventType.WARNING
                val descText = when {
                    tracking.isNotBlank() && description.isNotBlank() -> "박스 #$tracking · $description"
                    tracking.isNotBlank() -> "박스 #$tracking"
                    description.isNotBlank() -> description
                    else -> ""
                }
                addEvent(type, eventType, descText)
            }
            "defect_inspected" -> {
                val yoloResult = parseSummary(json, "yolo_result")
                val hasDefect = parseSummary(json, "has_defect")
                val isDefect = (yoloResult == "불량") || (hasDefect == "true")
                if (isDefect) {
                    val tracking = parseSummary(json, "tracking_number")
                        ?: parseSummary(json, "invoice_no")
                        ?: ""
                    val defectClass = parseSummary(json, "defect_class")
                    val descText = when {
                        tracking.isNotBlank() && defectClass != null -> "박스 #$tracking · $defectClass"
                        tracking.isNotBlank() -> "박스 #$tracking"
                        defectClass != null -> defectClass
                        else -> ""
                    }
                    addEvent(EventType.ERROR, "박스 훼손 감지", descText)
                }
            }
            "emergency_stop" -> {
                addEvent(EventType.ERROR, "비상 정지 발동됨", "관제 시스템")
                _uiState.update { it.copy(systemState = SystemState.STOPPED) }
            }
            "device_connected", "device_disconnected" -> {
                loadStatus()
            }
        }
    }

    private fun addEvent(type: EventType, title: String, description: String) {
        val newEvent = RecentEvent(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            description = description,
            timeAgo = "방금"
        )
        _uiState.update { state ->
            state.copy(recentEvents = (listOf(newEvent) + state.recentEvents).take(10))
        }
    }

    private fun parseSummary(json: String, field: String): String? {
        return try {
            val obj = JsonParser.parseString(json).asJsonObject
            if (obj.has(field) && !obj.get(field).isJsonNull) obj.get(field).asString else null
        } catch (_: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}

private fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
