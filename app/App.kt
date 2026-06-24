package com.example.sortingsystem

import android.app.Application
import android.content.Context

/**
 * 앱 전역에서 Application Context 가 필요할 때 안전하게 꺼내 쓰기 위한 클래스.
 *
 * DataStore 처럼 Context 가 필요한 곳에서 ViewModel 생성자를 복잡하게
 * 만들지 않고 App.context 로 바로 접근할 수 있게 해줍니다.
 *
 * ⚠️ AndroidManifest.xml 의 <application> 태그에
 *    android:name=".App" 를 추가해야 이 클래스가 실제로 사용됩니다.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()   // ← 추가
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "sorting_system_alerts",
                "시스템 알림",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "시스템 비상정지 등 중요 알림" }
            (getSystemService(android.app.NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }

    companion object {
        private lateinit var instance: App

        /** 앱 어디서나 사용할 수 있는 Application Context */
        val context: Context
            get() = instance.applicationContext
    }
}
