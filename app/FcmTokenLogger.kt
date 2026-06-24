package com.example.sortingsystem.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * 앱 시작 시 현재 FCM 토큰을 Logcat 에 출력하는 헬퍼.
 *
 * 처음 1회 발급되는 토큰은 onNewToken 으로 잡히지만, 그 이후 실행에서는
 * 호출되지 않으므로 이 헬퍼로 언제든 현재 토큰을 확인할 수 있게 합니다.
 *
 * Firebase 콘솔에서 테스트 알림을 보낼 때 이 토큰 값을 복사해서 넣습니다.
 */
object FcmTokenLogger {

    fun logCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM 토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                // 시각적으로 잘 보이도록 구분선과 함께 출력
                Log.d(TAG, "============= FCM TOKEN =============")
                Log.d(TAG, token)
                Log.d(TAG, "=====================================")
            }
    }

    private const val TAG = "FcmToken"
}
