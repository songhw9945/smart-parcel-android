package com.example.sortingsystem

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sortingsystem.fcm.FcmTokenLogger
import com.example.sortingsystem.ui.login.LoginScreen
import com.example.sortingsystem.ui.main.MainScreen
import com.example.sortingsystem.ui.theme.SortingSystemTheme
import com.example.sortingsystem.fcm.FcmTokenRegistrar

class MainActivity : ComponentActivity() {

    // Android 13+ 알림 권한 요청 launcher
    // 권한 허용/거부 결과는 콜백으로 들어오지만, 지금은 별도 처리 없이 결과만 받음.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        android.util.Log.d("MainActivity", "알림 권한: ${if (granted) "허용" else "거부"}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) 알림 권한 요청 (Android 13+ 만 필요)
        requestNotificationPermissionIfNeeded()

        // 2) 현재 FCM 토큰을 Logcat 에 출력
        //    → "FcmToken" 으로 검색해서 토큰 복사 → Firebase 콘솔에서 테스트 알림 시 사용
        FcmTokenLogger.logCurrentToken()
        FcmTokenRegistrar.registerCurrentToken()   // ← 추가

        setContent {
            SortingSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    SortingSystemApp()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Android 12 이하는 별도 권한 없이 알림 표시 가능
    }
}

@Composable
fun SortingSystemApp() {
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        MainScreen(onLogout = { isLoggedIn = false })
    } else {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true }
        )
    }
}
