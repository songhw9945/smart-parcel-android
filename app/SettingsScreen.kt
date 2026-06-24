package com.example.sortingsystem.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 설정 탭
 *
 * 구성:
 *   1. 계정 정보 (admin 표시)
 *   2. 알림 설정 - 오류 알림 / 일일 리포트 (2개)
 *   3. 서버 연결 - 주소 + 연결 상태
 *   4. 앱 정보 - 버전
 *   5. 로그아웃
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.background(Color.White)) {
        // 상단 헤더
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "설정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
        }
        HorizontalDivider(color = Color(0xFFF1EFE8), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 계정 정보
            AccountInfoCard(adminName = "admin", role = "관리자")
            Spacer(modifier = Modifier.height(20.dp))

            // ── 알림 설정 ─────────────────────────────────────────
            SectionTitle(text = "알림 설정")
            Spacer(modifier = Modifier.height(8.dp))

            SettingsToggleItem(
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFE24B4A),
                title = "오류 알림",
                description = "설비 오류 발생 시 즉시 알림",
                checked = state.errorAlertEnabled,
                onCheckedChange = { viewModel.setErrorAlertEnabled(it) }
            )
            Spacer(modifier = Modifier.height(6.dp))

            SettingsToggleItem(
                icon = Icons.Default.Description,
                iconColor = Color(0xFF534AB7),
                title = "일일 리포트",
                description = "매일 오전 9시 요약 알림",
                checked = state.dailyReportEnabled,
                onCheckedChange = { viewModel.setDailyReportEnabled(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 서버 연결 ─────────────────────────────────────────
            SectionTitle(text = "서버 연결")
            Spacer(modifier = Modifier.height(8.dp))

            InfoItem(
                icon = Icons.Default.Dns,
                iconColor = Color(0xFF0F6E56),
                title = "서버 주소",
                value = state.serverUrl
            )
            Spacer(modifier = Modifier.height(6.dp))

            ConnectionStatusItem(
                isOnline = state.isServerOnline,
                isChecking = state.isCheckingServer,
                onRefresh = { viewModel.checkServerConnection() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 앱 정보 ──────────────────────────────────────────
            SectionTitle(text = "앱 정보")
            Spacer(modifier = Modifier.height(8.dp))

            InfoItem(
                icon = Icons.Default.Info,
                iconColor = Color(0xFF888780),
                title = "버전",
                value = "1.0.0"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 로그아웃 버튼 ─────────────────────────────────────
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFCEBEB),
                    contentColor = Color(0xFFE24B4A)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFE24B4A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "로그아웃",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE24B4A)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

// ── 컴포넌트들 ────────────────────────────────────────────────────

@Composable
private fun AccountInfoCard(adminName: String, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE6F1FB))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF185FA5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${adminName}님",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF042C53)
            )
            Text(text = role, fontSize = 11.sp, color = Color(0xFF185FA5))
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

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Text(text = description, fontSize = 10.sp, color = Color(0xFF888780))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF185FA5),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD3D1C7)
            )
        )
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    valueColor: Color = Color(0xFF1A1A1A)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun ConnectionStatusItem(
    isOnline: Boolean,
    isChecking: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F6))
            .clickable(enabled = !isChecking) { onRefresh() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = if (isOnline) Color(0xFF0F6E56) else Color(0xFF888780),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "연결 상태",
            fontSize = 13.sp,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        if (isChecking) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color(0xFF888780),
                modifier = Modifier.size(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF1D9E75) else Color(0xFFE24B4A))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "정상" else "연결 끊김",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isOnline) Color(0xFF0F6E56) else Color(0xFFA32D2D)
            )
        }
    }
}

@Composable
private fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFF888780),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "로그아웃",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "로그아웃 하시겠습니까?",
                    fontSize = 13.sp,
                    color = Color(0xFF5F5E5A)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFFD3D1C7)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Text("취소", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE24B4A)
                        )
                    ) {
                        Text(
                            "로그아웃",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
