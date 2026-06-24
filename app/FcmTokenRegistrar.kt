package com.example.sortingsystem.fcm

import android.util.Log
import com.example.sortingsystem.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 현재 FCM 토큰을 서버에 POST 해서 등록한다.
 * 서버 엔드포인트: POST {SERVER_URL}/api/fcm/register-token  body: {"token":"..."}
 */
object FcmTokenRegistrar {

    /** 앱 실행 시 호출 — 현재 토큰을 가져와 서버에 등록 */
    fun registerCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                }
                postToken(task.result)
            }
    }

    /** onNewToken 콜백에서 호출 — 갱신된 토큰을 서버에 등록 */
    fun postToken(token: String) {
        thread {
            try {
                val url = URL("${BuildConfig.SERVER_URL}/api/fcm/register-token")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                conn.outputStream.use { it.write(JSONObject().put("token", token).toString().toByteArray()) }
                Log.d(TAG, "토큰 등록 응답 코드: ${conn.responseCode}")
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "토큰 등록 실패", e)
            }
        }
    }

    private const val TAG = "FcmTokenReg"
}