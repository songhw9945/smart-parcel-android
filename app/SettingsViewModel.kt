package com.example.sortingsystem.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sortingsystem.App
import com.example.sortingsystem.BuildConfig
import com.example.sortingsystem.data.SettingsPreferences
import com.example.sortingsystem.data.network.ApiResult
import com.example.sortingsystem.data.network.SortingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 설정 화면 ViewModel
 *
 * 역할:
 *  - 알림 옵션(오류 알림 / 일일 리포트) 저장·불러오기 → DataStore (영구 저장)
 *  - 서버 주소 표시 (BuildConfig.SERVER_URL)
 *  - 서버 연결 상태 확인 (/api/status 호출)
 *
 * ※ 이전에 AndroidViewModel(application) 로 만들었더니 Compose 의 기본
 *   viewModel() 이 인스턴스를 생성하지 못해 설정 탭 진입 시 크래시가 났습니다.
 *   그래서 일반 ViewModel 로 되돌리고, Context 가 필요한 DataStore 는
 *   App.context (전역 Application Context) 를 사용하도록 바꿨습니다.
 */
class SettingsViewModel(
    private val repository: SortingRepository = SortingRepository()
) : ViewModel() {

    // 생성자 인자가 없어졌으므로 viewModel() 이 문제없이 생성할 수 있습니다.
    private val prefs = SettingsPreferences(App.context)

    data class UiState(
        val errorAlertEnabled: Boolean = true,
        val dailyReportEnabled: Boolean = false,
        val serverUrl: String = BuildConfig.SERVER_URL,
        val isServerOnline: Boolean = false,
        val isCheckingServer: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeStoredSettings()
        checkServerConnection()
    }

    private fun observeStoredSettings() {
        viewModelScope.launch {
            prefs.errorAlertFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(errorAlertEnabled = enabled)
            }
        }
        viewModelScope.launch {
            prefs.dailyReportFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(dailyReportEnabled = enabled)
            }
        }
    }

    fun setErrorAlertEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setErrorAlert(enabled) }
    }

    fun setDailyReportEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setDailyReport(enabled) }
    }

    fun checkServerConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingServer = true)
            val result = repository.fetchStatus()
            _uiState.value = _uiState.value.copy(
                isCheckingServer = false,
                isServerOnline = result is ApiResult.Success
            )
        }
    }
}
