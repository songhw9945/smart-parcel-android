package com.example.sortingsystem.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortingsystem.data.CameraInfo
import com.example.sortingsystem.data.CameraRegistry

/**
 * 카메라 탭
 *
 * 기본: 카메라 5개를 한 화면에 세로로 나열 (각 카드 우측에 확대 버튼)
 * 확대: 카드를 탭하거나 확대 버튼 누르면 한 카메라를 큰 화면으로 보기 (뒤로가기로 복귀)
 *
 * URL 이 비어있는 카메라는 자동으로 "준비 중" 표시됩니다.
 */
@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    var fullscreenCamera by remember { mutableStateOf<CameraInfo?>(null) }

    if (fullscreenCamera != null) {
        FullscreenCameraView(
            camera = fullscreenCamera!!,
            onBack = { fullscreenCamera = null },
            modifier = modifier
        )
    } else {
        CameraListView(
            cameras = CameraRegistry.cameras,
            onCameraClick = { fullscreenCamera = it },
            modifier = modifier
        )
    }
}

@Composable
private fun CameraListView(
    cameras: List<CameraInfo>,
    onCameraClick: (CameraInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Color.White)) {
        // 헤더
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "카메라",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(cameras, key = { it.id }) { camera ->
                CameraCard(camera = camera, onClick = { onCameraClick(camera) })
            }
        }
    }
}

@Composable
private fun CameraCard(camera: CameraInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // 카메라 이름 + 확대 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = camera.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "확대",
                tint = Color(0xFF888780),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 스트림 영상 (16:9 비율)
        CameraStreamView(
            url = camera.url,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )
    }
}

@Composable
private fun FullscreenCameraView(
    camera: CameraInfo,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Color.Black)) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color.White
                )
            }
            Text(
                text = camera.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        // 큰 화면 - 남은 공간 전부 사용
        CameraStreamView(
            url = camera.url,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
