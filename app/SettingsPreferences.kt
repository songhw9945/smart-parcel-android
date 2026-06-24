package com.example.sortingsystem.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 앱 설정값을 기기에 영구 저장/불러오기 하는 클래스
 *
 * DataStore 를 사용해 토글 상태(오류 알림 / 일일 리포트)를 저장합니다.
 * 앱을 완전히 종료했다 다시 켜도 마지막으로 설정한 값이 유지됩니다.
 *
 * - 읽기: errorAlertFlow / dailyReportFlow (Flow 라서 값이 바뀌면 자동으로 흘러나옴)
 * - 쓰기: setErrorAlert() / setDailyReport()
 */

// Context 에 단 하나의 DataStore 인스턴스를 연결 (파일명: settings)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        // 저장에 쓸 키. 이 이름으로 값이 파일에 기록됩니다.
        private val KEY_ERROR_ALERT = booleanPreferencesKey("error_alert_enabled")
        private val KEY_DAILY_REPORT = booleanPreferencesKey("daily_report_enabled")

        // 처음 설치했을 때(저장된 값이 없을 때)의 기본값
        private const val DEFAULT_ERROR_ALERT = true
        private const val DEFAULT_DAILY_REPORT = false
    }

    /** 오류 알림 ON/OFF 값 (없으면 기본값 true) */
    val errorAlertFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ERROR_ALERT] ?: DEFAULT_ERROR_ALERT
    }

    /** 일일 리포트 ON/OFF 값 (없으면 기본값 false) */
    val dailyReportFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DAILY_REPORT] ?: DEFAULT_DAILY_REPORT
    }

    suspend fun setErrorAlert(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ERROR_ALERT] = enabled
        }
    }

    suspend fun setDailyReport(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_REPORT] = enabled
        }
    }
}
