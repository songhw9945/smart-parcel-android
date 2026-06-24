package com.example.sortingsystem.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sortingsystem.ui.camera.CameraScreen
import com.example.sortingsystem.ui.home.HomeScreen
import com.example.sortingsystem.ui.settings.SettingsScreen

/**
 * 메인 화면 - 로그인 성공 후 진입하는 화면
 *
 * 하단 네비게이션 바로 3개 탭(홈/카메라/설정)을 전환
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF185FA5),
                            selectedTextColor = Color(0xFF185FA5),
                            unselectedIconColor = Color(0xFF888780),
                            unselectedTextColor = Color(0xFF888780),
                            indicatorColor = Color(0xFFE6F1FB)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.HOME -> HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            MainTab.CAMERA -> CameraScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            MainTab.SETTINGS -> SettingsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onLogout = onLogout
            )
        }
    }
}
