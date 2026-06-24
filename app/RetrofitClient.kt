package com.example.sortingsystem.data.network

import com.example.sortingsystem.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit / OkHttp 인스턴스를 한 곳에서 만들어 제공하는 객체
 *
 * object 키워드 = 앱 전체에서 단 하나만 존재하는 싱글톤.
 * 화면마다 새로 만들 필요 없이 RetrofitClient.api 로 바로 꺼내 씁니다.
 *
 * 서버 주소는 build.gradle.kts 의 BuildConfig.SERVER_URL 에서 가져옵니다.
 * (현재: http://192.168.0.21:5000 — 서버 IP 바뀌면 gradle 파일만 수정)
 */
object RetrofitClient {

    private const val BASE_URL = BuildConfig.SERVER_URL

    // 통신 내용을 Logcat 에 찍어주는 인터셉터. 디버깅할 때 요청/응답을 눈으로 확인 가능.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: SortingApi by lazy {
        Retrofit.Builder()
            // BASE_URL 은 반드시 / 로 끝나야 합니다. (Retrofit 규칙)
            .baseUrl(if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SortingApi::class.java)
    }
}
