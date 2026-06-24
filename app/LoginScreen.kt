package com.example.sortingsystem.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 관리자 로그인 화면
 *
 * @param onLoginSuccess 로그인 성공 시 호출되는 콜백 (메인 화면으로 이동)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    // 입력값 상태 관리
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 임시 관리자 계정 (나중에 서버 API 연동으로 대체)
    val validAdminAccounts = mapOf(
        "admin" to "admin1234",
        "manager" to "manager1234"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고 영역
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFE6F1FB),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📦",
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 타이틀
            Text(
                text = "분류 시스템 관제",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "관리자 로그인",
                fontSize = 14.sp,
                color = Color(0xFF888780)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 아이디 입력 필드
            OutlinedTextField(
                value = userId,
                onValueChange = {
                    userId = it
                    errorMessage = null
                },
                label = { Text("아이디") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "아이디",
                        tint = Color(0xFF888780)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185FA5),
                    unfocusedBorderColor = Color(0xFFD3D1C7)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력 필드
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("비밀번호") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "비밀번호",
                        tint = Color(0xFF888780)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                            tint = Color(0xFF888780)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185FA5),
                    unfocusedBorderColor = Color(0xFFD3D1C7)
                )
            )

            // 에러 메시지 표시
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFE24B4A),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인 버튼
            Button(
                onClick = {
                    when {
                        userId.isBlank() -> {
                            errorMessage = "아이디를 입력해주세요"
                        }
                        password.isBlank() -> {
                            errorMessage = "비밀번호를 입력해주세요"
                        }
                        else -> {
                            isLoading = true
                            // 로그인 검증 (현재는 임시 계정으로 체크, 추후 서버 API 연동)
                            val storedPassword = validAdminAccounts[userId]
                            if (storedPassword != null && storedPassword == password) {
                                isLoading = false
                                onLoginSuccess()
                            } else {
                                isLoading = false
                                errorMessage = "등록되지 않은 관리자이거나 비밀번호가 일치하지 않습니다"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF185FA5),
                    disabledContainerColor = Color(0xFFB5D4F4)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "로그인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "등록된 관리자만 접근할 수 있습니다",
                fontSize = 12.sp,
                color = Color(0xFF888780)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "v1.0.0 · 택배 자동 분류 시스템",
                fontSize = 11.sp,
                color = Color(0xFFB4B2A9)
            )
        }
    }
}
