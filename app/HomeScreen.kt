package com.example.sortingsystem.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.example.sortingsystem.data.Equipment
import com.example.sortingsystem.data.EquipmentState
import com.example.sortingsystem.data.EquipmentType
import com.example.sortingsystem.data.EventType
import com.example.sortingsystem.data.RecentEvent
import com.example.sortingsystem.data.SystemState

/**
 * 홈 탭 - 모니터링 대시보드 (읽기 전용)
 *
 * ⚠️ 이 앱은 장비를 제어하지 않습니다. 상태/이벤트를 "보기"만 합니다.
 *
 * 구성 (위에서 아래로):
 *   1. 상단 헤더    : 관리자 인사 + 시스템 상태 뱃지 + 새로고침
 *   2. 시스템 상태 배너 : 현재 전체 상태를 크게 표시 (정상/오류/정지)
 *   3. 오늘 통계    : "오늘 N건 · 오류 N건" 한 줄
 *   4. 설비 상태   : "전체 정상" 한 줄, 오류 시 자동 펼침/탭 토글
 *   5. 최근 이벤트 : 최대 10개 (Socket.IO 로 실시간 누적)
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    Column(
        modifier = modifier
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        TopHeader(
            adminName = "admin",
            systemState = state.systemState,
            isLoading = state.isLoading,
            onRefresh = { viewModel.loadStatus() }
        )
        Spacer(modifier = Modifier.height(14.dp))

        // 시스템 상태 배너 (비상정지 버튼이 있던 자리 → 상태 표시로 대체)
        SystemStatusBanner(systemState = state.systemState)
        Spacer(modifier = Modifier.height(16.dp))

        // 오늘 통계
        TodaySummaryLine(sorted = state.todaySorted, error = state.todayError)
        Spacer(modifier = Modifier.height(20.dp))

        // 설비 상태
        EquipmentSection(
            equipmentList = state.equipmentList,
            errorCount = state.errorCount
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 최근 이벤트
        SectionTitle(text = "최근 이벤트")
        Spacer(modifier = Modifier.height(8.dp))
        if (state.recentEvents.isEmpty()) {
            EmptyEventsHint(isLoading = state.isLoading)
        } else {
            state.recentEvents.forEach { event ->
                RecentEventCard(event = event)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── 상단 헤더 (새로고침 버튼 포함) ────────────────────────────────
@Composable
private fun TopHeader(
    adminName: String,
    systemState: SystemState,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val (badgeBg, dotColor, textColor) = statusColors(systemState)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("관리자", fontSize = 11.sp, color = Color(0xFF888780))
            Text(
                "${adminName}님",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = systemState.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // 새로고침 버튼 (모니터링 앱이므로 수동 갱신 제공)
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.size(32.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color(0xFF888780),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = Color(0xFF888780),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── 시스템 상태 배너 ──────────────────────────────────────────────
@Composable
private fun SystemStatusBanner(systemState: SystemState) {
    val (bg, accent, _) = statusColors(systemState)
    val icon = when (systemState) {
        SystemState.NORMAL -> Icons.Default.CheckCircle
        SystemState.WARNING -> Icons.Default.Warning
        SystemState.ERROR -> Icons.Default.Error
        SystemState.STOPPED -> Icons.Default.DoNotDisturbOn
    }
    val message = when (systemState) {
        SystemState.NORMAL -> "시스템이 정상 가동 중입니다"
        SystemState.WARNING -> "주의가 필요한 상태입니다"
        SystemState.ERROR -> "오류가 발생했습니다"
        SystemState.STOPPED -> "시스템이 정지된 상태입니다"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = systemState.displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = message,
                fontSize = 11.sp,
                color = accent.copy(alpha = 0.8f)
            )
        }
    }
}

// ── 오늘 통계 (한 줄) ─────────────────────────────────────────────
@Composable
private fun TodaySummaryLine(sorted: Int, error: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F8F6))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Today,
            contentDescription = null,
            tint = Color(0xFF888780),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("오늘 분류", fontSize = 12.sp, color = Color(0xFF888780))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "${sorted}건",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFD3D1C7))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("오류", fontSize = 12.sp, color = Color(0xFF888780))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "${error}건",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (error > 0) Color(0xFFA32D2D) else Color(0xFF1A1A1A)
        )
    }
}

// ── 설비 상태 (펼치기) ────────────────────────────────────────────
@Composable
private fun EquipmentSection(equipmentList: List<Equipment>, errorCount: Int) {
    var expanded by remember(errorCount) { mutableStateOf(errorCount > 0) }
    val canToggle = equipmentList.isNotEmpty()

    SectionTitle(text = "설비 상태")
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .then(if (canToggle) Modifier.clickable { expanded = !expanded } else Modifier)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (errorCount > 0) Color(0xFFE24B4A) else Color(0xFF1D9E75))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (errorCount > 0) "오류 ${errorCount}개" else "전체 정상",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (errorCount > 0) Color(0xFFA32D2D) else Color(0xFF085041),
            modifier = Modifier.weight(1f)
        )
        if (canToggle) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = Color(0xFF888780),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            Spacer(modifier = Modifier.height(6.dp))
            equipmentList.forEach { equipment ->
                EquipmentDetailCard(equipment = equipment)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun EquipmentDetailCard(equipment: Equipment) {
    val icon = when (equipment.type) {
        EquipmentType.QR_OCR_CAMERA -> Icons.Default.CameraAlt
        EquipmentType.ROBOT_ARM -> Icons.Default.PrecisionManufacturing
        EquipmentType.SORTING_CAMERA -> Icons.Default.Videocam
        EquipmentType.CONVEYOR -> Icons.Default.LinearScale
    }
    val iconColor = when (equipment.type) {
        EquipmentType.QR_OCR_CAMERA -> Color(0xFF185FA5)
        EquipmentType.ROBOT_ARM -> Color(0xFF534AB7)
        EquipmentType.SORTING_CAMERA -> Color(0xFF993C1D)
        EquipmentType.CONVEYOR -> Color(0xFF888780)
    }
    val stateColor = when (equipment.state) {
        EquipmentState.NORMAL, EquipmentState.OPERATING, EquipmentState.RECORDING -> Color(0xFF1D9E75)
        EquipmentState.WARNING -> Color(0xFFBA7517)
        EquipmentState.ERROR -> Color(0xFFE24B4A)
        EquipmentState.OFFLINE -> Color(0xFF888780)
    }
    val stateTextColor = when (equipment.state) {
        EquipmentState.NORMAL, EquipmentState.OPERATING, EquipmentState.RECORDING -> Color(0xFF0F6E56)
        EquipmentState.WARNING -> Color(0xFF854F0B)
        EquipmentState.ERROR -> Color(0xFFA32D2D)
        EquipmentState.OFFLINE -> Color(0xFF5F5E5A)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = equipment.type.displayName,
            fontSize = 13.sp,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(stateColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = equipment.state.displayName,
            fontSize = 11.sp,
            color = stateTextColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── 최근 이벤트 ───────────────────────────────────────────────────
@Composable
private fun EmptyEventsHint(isLoading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color(0xFF888780),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("불러오는 중...", fontSize = 12.sp, color = Color(0xFF888780))
        } else {
            Text("아직 이벤트가 없습니다.", fontSize = 12.sp, color = Color(0xFFB4B2A9))
        }
    }
}

@Composable
private fun RecentEventCard(event: RecentEvent) {
    val bgColor = when (event.type) {
        EventType.SUCCESS -> Color(0xFFEAF3DE)
        EventType.WARNING -> Color(0xFFFAEEDA)
        EventType.ERROR -> Color(0xFFFCEBEB)
        EventType.INFO -> Color(0xFFE6F1FB)
    }
    val iconColor = when (event.type) {
        EventType.SUCCESS -> Color(0xFF639922)
        EventType.WARNING -> Color(0xFFBA7517)
        EventType.ERROR -> Color(0xFFE24B4A)
        EventType.INFO -> Color(0xFF185FA5)
    }
    val textColor = when (event.type) {
        EventType.SUCCESS -> Color(0xFF27500A)
        EventType.WARNING -> Color(0xFF633806)
        EventType.ERROR -> Color(0xFF791F1F)
        EventType.INFO -> Color(0xFF042C53)
    }
    val icon: ImageVector = when (event.type) {
        EventType.SUCCESS -> Icons.Default.CheckCircle
        EventType.WARNING -> Icons.Default.Warning
        EventType.ERROR -> Icons.Default.Error
        EventType.INFO -> Icons.Default.Info
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = if (event.description.isBlank()) event.timeAgo
                    else "${event.timeAgo} · ${event.description}",
                    fontSize = 10.sp,
                    color = textColor
                )
            }
        }

        // 박스불량 등 이미지가 있는 이벤트만 사진 표시 (Vision실패/비상정지는 imageUrl = null)
        if (!event.imageUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            coil.compose.AsyncImage(
                model = event.imageUrl,
                contentDescription = "이벤트 이미지",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF5F5E5A),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

/** 시스템 상태별 (배경색, 강조색, 텍스트색) 묶음 */
private fun statusColors(systemState: SystemState): Triple<Color, Color, Color> = when (systemState) {
    SystemState.NORMAL -> Triple(Color(0xFFE1F5EE), Color(0xFF1D9E75), Color(0xFF085041))
    SystemState.WARNING -> Triple(Color(0xFFFAEEDA), Color(0xFFBA7517), Color(0xFF633806))
    SystemState.ERROR -> Triple(Color(0xFFFCEBEB), Color(0xFFE24B4A), Color(0xFF791F1F))
    SystemState.STOPPED -> Triple(Color(0xFFFCEBEB), Color(0xFFE24B4A), Color(0xFF791F1F))
}
