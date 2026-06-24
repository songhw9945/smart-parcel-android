package com.example.sortingsystem.ui.camera

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * MJPEG 스트림을 보여주는 뷰
 *
 * WebView 에 스트림 URL 을 그대로 띄우는 방식입니다.
 *   - 안드로이드 WebView 는 multipart/x-mixed-replace (MJPEG) 를 자체 지원합니다.
 *   - 브라우저에서 URL 을 열었을 때 영상이 뜨는 경우, 같은 방식으로 동작합니다.
 *
 * URL 이 비어있으면 "준비 중" 플레이스홀더를 표시합니다.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CameraStreamView(
    url: String,
    modifier: Modifier = Modifier
) {
    if (url.isBlank()) {
        // URL 미확정 카메라 → 준비 중 표시
        PlaceholderBox(text = "준비 중", modifier = modifier)
        return
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.BLACK)
                    webViewClient = WebViewClient()
                    settings.apply {
                        javaScriptEnabled = false        // MJPEG 만 보면 되므로 JS 비활성
                        loadWithOverviewMode = true      // 영상이 화면에 맞게 축소되어 표시
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    // MJPEG 자체를 HTML 페이지 없이 곧바로 보여주기 위해 img 태그로 감쌉니다.
                    // (브라우저에 raw stream URL 을 직접 띄우면 일부 환경에서 안 보임)
                    val html = """
                        <html>
                          <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <style>
                              html, body { margin:0; padding:0; background:#000;
                                           height:100%; display:flex;
                                           align-items:center; justify-content:center; }
                              img { width:100%; height:auto; display:block; }
                            </style>
                          </head>
                          <body><img src="$url" /></body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL(url, html, "text/html", "utf-8", null)
                }
            },
            update = { webView ->
                // URL 이 바뀌면 재로드 (현재는 고정 URL 이지만 추후 변경 가능)
                val html = """
                    <html><body style="margin:0;background:#000;">
                      <img src="$url" style="width:100%;height:auto;" />
                    </body></html>
                """.trimIndent()
                webView.loadDataWithBaseURL(url, html, "text/html", "utf-8", null)
            }
        )
    }
}

@Composable
private fun PlaceholderBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1EFE8)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.VideocamOff,
                contentDescription = null,
                tint = Color(0xFFB4B2A9),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                color = Color(0xFF888780),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
