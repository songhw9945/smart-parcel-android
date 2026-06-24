package com.example.sortingsystem.data.network

import android.util.Log
import com.example.sortingsystem.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Flask-SocketIO 서버와의 실시간 연결을 관리하는 클래스
 *
 * 구독 이벤트:
 *   - blackbox_event_added : 박스 훼손 검출 → ERROR 이벤트로 화면 표시
 *                              (현재 서버가 이쪽으로 쏘고 있음)
 *   - defect_inspected     : v6.0 명세상의 박스 훼손 이벤트
 *                              (서버가 아직 안 쏘지만, 추후 쏠 경우 대비해 구독 유지)
 *   - emergency_stop       : 비상정지 발동
 *   - device_connected     : 장비 연결 → 설비 상태 카드 갱신
 *   - device_disconnected  : 장비 끊김 → 설비 상태 카드 갱신
 *
 * 화면에 표시할 데가 없는 이벤트들(package_scanned, sort_completed, car_* 등)은
 * 의도적으로 구독하지 않습니다.
 */
class RealtimeSocketManager {

    enum class ConnectionState { CONNECTED, DISCONNECTED, CONNECTING }

    /** 실시간으로 들어온 이벤트 한 건 */
    data class RealtimeEvent(val name: String, val rawJson: String)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private var socket: Socket? = null

    private val subscribedEvents = listOf(
        "blackbox_event_added",   // 박스 훼손 (현재 서버가 쏘는 이벤트)
        "defect_inspected",       // 박스 훼손 (v6.0 명세, 추후 활성화 대비)
        "emergency_stop",
        "device_connected",
        "device_disconnected"
    )

    fun connect() {
        if (socket != null) return

        _connectionState.value = ConnectionState.CONNECTING
        try {
            val opts = IO.Options().apply {
                transports = arrayOf("polling", "websocket")  // polling 먼저, websocket 업그레이드
            }
            val s = IO.socket(BuildConfig.SERVER_URL, opts)
            socket = s

            s.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket.IO 연결 성공 / sid=${s.id()}")
                _connectionState.value = ConnectionState.CONNECTED
            }
            s.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket.IO 연결 끊김")
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.w(TAG, "Socket.IO 연결 오류: ${args.firstOrNull()}")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            subscribedEvents.forEach { eventName ->
                s.on(eventName) { args ->
                    val payload = args.firstOrNull()?.toString() ?: "{}"
                    Log.d(TAG, "[$eventName] $payload")
                    _events.tryEmit(RealtimeEvent(eventName, payload))
                }
            }

            s.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket.IO 초기화 실패: ${e.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    fun disconnect() {
        socket?.let {
            it.off()
            it.disconnect()
        }
        socket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    companion object {
        private const val TAG = "RealtimeSocket"
    }
}
