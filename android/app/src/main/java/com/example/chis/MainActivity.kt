package com.example.chis

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chis.pages.IndexScreen
import com.example.chis.pages.MainScreen
import com.example.chis.ui.theme.ChisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. 初始化本地 SharedPreferences 存储
        val sharedPrefs = getSharedPreferences("chis_user_prefs", Context.MODE_PRIVATE)

        setContent {
            ChisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. 从本地缓存中读取保存的登录状态（默认 false）
                    var isLoggedIn by remember {
                        mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false))
                    }

                    // 3. 根据登录状态实时切换视图
                    if (isLoggedIn) {
                        // 已登录：显示主界面，并传入退出登录回调
                        MainScreen(
                            onLogOut = {
                                // 擦除本地登录标记，并更新 Compose 状态切回登录页
                                sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        // 未登录：显示登录注册页
                        IndexScreen(
                            onLoginSuccess = {
                                // 登录成功：持久化保存登录标记为 true，并切到主界面
                                sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
}