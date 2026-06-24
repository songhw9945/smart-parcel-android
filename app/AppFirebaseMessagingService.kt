package com.example.sortingsystem.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sortingsystem.MainActivity
import com.example.sortingsystem.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM (Firebase Cloud Messaging) 수신 서비스
 *
 * 두 가지를 처리합니다:
 *   1) onNewToken : 토큰이 새로 발급되거나 갱신될 때 호출.
 *      → 서버에 토큰을 등록해야 푸시 알림이 그 기기로 전달됩니다.
 *      → 지금은 서버 연동(B단계) 전이라 Logcat 에 출력만 합니다.
 *
 *   2) onMessageReceived : 앱이 포그라운드일 때 메시지 수신.
 *      → 알림 채널/알림으로 변환해 사용자에게 표시.
 *
 * 백그라운드일 때는 FCM 이 알아서 알림을 띄우므로 이 메서드가 호출되지 않습니다.
 *
 * ⚠️ AndroidManifest.xml 에 이 서비스를 등록해야 동작합니다.
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * 새 토큰이 발급되면 호출됩니다.
     * 예) 앱 첫 실행, 앱 데이터 삭제 후 재실행, 토큰 만료 시 갱신 등.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM 토큰 갱신: $token")
        FcmTokenRegistrar.postToken(token)   // ← 추가
        // TODO(B단계): 서버에 이 토큰을 POST 해서 저장 시키기.
        //   예) repository.registerFcmToken(token)
        //   지금은 Logcat 출력만 - Firebase 콘솔에서 테스트 알림 쏠 때 이 토큰 사용.
    }

    /**
     * 앱이 포그라운드일 때 FCM 메시지를 받으면 호출됩니다.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // FCM 메시지에는 notification 페이로드(자동 알림용)와
        // data 페이로드(앱이 직접 처리하는 키-값)가 있습니다.
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "알림"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        Log.d(TAG, "메시지 수신: title=$title, body=$body")
        showNotification(title, body)
    }

    /** 시스템 알림으로 표시 */
    private fun showNotification(title: String, body: String) {
        // 알림 채널 보장 (Android 8+ 필수)
        ensureChannel()

        // 알림 클릭 시 앱이 열리도록 PendingIntent 구성
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)  // 앱 아이콘 사용 (별도 알림 아이콘 만들지 않음)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Android 13+ 에서는 POST_NOTIFICATIONS 권한이 있어야 알림이 보임
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (hasPermission) {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } else {
            Log.w(TAG, "알림 권한 없음 - 알림 표시 생략")
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "시스템 비상정지 등 중요 알림"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "AppFCM"
        const val CHANNEL_ID = "sorting_system_alerts"
        const val CHANNEL_NAME = "시스템 알림"
        private const val NOTIFICATION_ID = 1001
    }
}
