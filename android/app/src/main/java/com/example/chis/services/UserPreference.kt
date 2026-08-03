package com.example.chis.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chis_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REGISTERED_EMAIL = "registered_email"
        private const val KEY_REGISTERED_PASSWORD = "registered_password"
    }

    // 1. 登录状态控制
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    // 2. 记住新注册的邮箱与密码
    fun saveRegisteredUser(email: String, pass: String) {
        prefs.edit()
            .putString(KEY_REGISTERED_EMAIL, email)
            .putString(KEY_REGISTERED_PASSWORD, pass)
            .apply()
    }

    fun getRegisteredEmail(): String = prefs.getString(KEY_REGISTERED_EMAIL, "") ?: ""
    fun getRegisteredPassword(): String = prefs.getString(KEY_REGISTERED_PASSWORD, "") ?: ""

    // 3. 清除登录状态（退出登录时调用）
    fun clearLoginState() {
        isLoggedIn = false
    }
}