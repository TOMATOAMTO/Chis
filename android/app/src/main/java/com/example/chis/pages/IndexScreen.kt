package com.example.chis.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chis.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

// 1. 枚举状态 (对应 ActiveView)
enum class ActiveView {
  LANDING,          // 落地页
  SIGNUP_OPTIONS,   // 注册选项
  LOGIN_OPTIONS,    // 登录选项
  FORM_LOGIN,       // 已有账号(用户名)登录
  EMAIL_SIGNUP,     // 电子邮件注册
  EMAIL_LOGIN       // 电子邮件登录
}

@Composable
fun IndexScreen(
  onLoginSuccess: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // ---------- 本地 SharedPreferences 存储客户端 ----------
  val sharedPrefs = remember {
    context.getSharedPreferences("chis_user_prefs", Context.MODE_PRIVATE)
  }

  // ---------- 2. State 状态定义 ----------
  var activeView by remember { mutableStateOf(ActiveView.LANDING) }
  var animDirection by remember { mutableIntStateOf(1) } // 1: 前进, -1: 后退

  // 表单数据 (用户名/已有账号)
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  // 电子邮件表单数据
  var email by remember { mutableStateOf("") }
  var emailPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  // 密码可见性
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isEmailPasswordVisible by remember { mutableStateOf(false) }
  var isConfirmPasswordVisible by remember { mutableStateOf(false) }

  // 交互与焦点
  var isError by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }

  var usernameFocus by remember { mutableStateOf(false) }
  var passwordFocus by remember { mutableStateOf(false) }
  var emailFocus by remember { mutableStateOf(false) }
  var emailPasswordFocus by remember { mutableStateOf(false) }
  var confirmPasswordFocus by remember { mutableStateOf(false) }

  // 静态默认测试数据
  val correctUsername = "Liuweishuang"
  val correctPassword = "000000"
  val correctEmail = "liuweishuang@qingshi.com"

  // ---------- 3. 业务辅助逻辑 ----------
  fun navigate(target: ActiveView, forward: Boolean = true) {
    isError = false
    animDirection = if (forward) 1 else -1
    activeView = target
  }

  fun handleBack() {
    when (activeView) {
      ActiveView.SIGNUP_OPTIONS, ActiveView.LOGIN_OPTIONS -> navigate(ActiveView.LANDING, false)
      ActiveView.FORM_LOGIN, ActiveView.EMAIL_LOGIN -> navigate(ActiveView.LOGIN_OPTIONS, false)
      ActiveView.EMAIL_SIGNUP -> navigate(ActiveView.SIGNUP_OPTIONS, false)
      else -> {}
    }
  }

  fun isEmailValid(emailStr: String): Boolean {
    val pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    return pattern.matcher(emailStr).matches()
  }

  fun handleLogin() {
    isError = false
    if (username.trim().isEmpty() || password.trim().isEmpty()) {
      isError = true
      Toast.makeText(context, "用户名或密码不能为空", Toast.LENGTH_SHORT).show()
      return
    }

    isLoading = true
    scope.launch {
      delay(1200)
      if (username == correctUsername && password == correctPassword) {
        isLoading = false
        Toast.makeText(context, "登录成功，正在进入植宠空间！", Toast.LENGTH_SHORT).show()
        delay(800)
        onLoginSuccess()
      } else {
        isLoading = false
        isError = true
        Toast.makeText(context, "用户名或密码不正确", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun handleEmailLogin() {
    isError = false
    if (!isEmailValid(email) || emailPassword.length < 6) {
      isError = true
      Toast.makeText(context, "请输入有效的邮箱和6位以上密码", Toast.LENGTH_SHORT).show()
      return
    }

    isLoading = true
    scope.launch {
      delay(1200)
      // 从 SharedPreferences 中实时获取上次新注册的邮箱和密码
      val registeredEmail = sharedPrefs.getString("registered_email", "") ?: ""
      val registeredPassword = sharedPrefs.getString("registered_password", "") ?: ""

      val isDefaultValid = (email == correctEmail && emailPassword == correctPassword)
      val isRegisteredValid = (registeredEmail.isNotEmpty() && email == registeredEmail && emailPassword == registeredPassword)

      if (isDefaultValid || isRegisteredValid) {
        isLoading = false
        Toast.makeText(context, "邮箱登录成功，欢迎来到青莳！", Toast.LENGTH_SHORT).show()
        delay(800)
        onLoginSuccess()
      } else {
        isLoading = false
        isError = true
        Toast.makeText(context, "邮箱或密码不匹配", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun handleEmailSignup() {
    isError = false
    if (!isEmailValid(email)) {
      isError = true
      Toast.makeText(context, "请输入有效的电子邮箱地址", Toast.LENGTH_SHORT).show()
      return
    }
    if (emailPassword.length < 6) {
      isError = true
      Toast.makeText(context, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
      return
    }
    if (emailPassword != confirmPassword) {
      isError = true
      Toast.makeText(context, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
      return
    }

    isLoading = true
    scope.launch {
      delay(1500)
      isLoading = false

      // ✅ 关键持久化写入：将注册的邮箱与密码存入 SharedPreferences
      sharedPrefs.edit()
        .putString("registered_email", email)
        .putString("registered_password", emailPassword)
        .apply()

      Toast.makeText(context, "注册成功！请使用该邮箱登录。", Toast.LENGTH_LONG).show()
      navigate(ActiveView.EMAIL_LOGIN, true)
    }
  }

  // ---------- 4. 全局背景与视图渲染 ----------
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF1E5F41))
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp)
    ) {
      AnimatedContent(
        targetState = activeView,
        transitionSpec = {
          val slideDirection = animDirection
          val enter = slideInHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> if (slideDirection > 0) fullWidth else -fullWidth }
          ) + fadeIn(animationSpec = tween(240))

          val exit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> if (slideDirection > 0) -fullWidth else fullWidth }
          ) + fadeOut(animationSpec = tween(240))

          enter togetherWith exit
        },
        label = "LoginViewTransition"
      ) { targetState ->
        when (targetState) {
          // ==================== 1. LANDING (落地首屏) ====================
          ActiveView.LANDING -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "开启青莳，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "守护方寸生机。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 65.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Button(
                  onClick = { navigate(ActiveView.SIGNUP_OPTIONS, true) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Text(
                    text = "免费注册",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E5F41)
                  )
                }

                Button(
                  onClick = { navigate(ActiveView.LOGIN_OPTIONS, true) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(25.dp)),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Text(
                    text = "登录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }

          // ==================== 2. SIGNUP_OPTIONS (注册选择) ====================
          ActiveView.SIGNUP_OPTIONS -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "连接智能花盆，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "聆听花的呼吸。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 65.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Button(
                  onClick = { navigate(ActiveView.EMAIL_SIGNUP, true) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(25.dp)),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B7B58)),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                      painter = painterResource(id = R.drawable.email),
                      contentDescription = "Email",
                      colorFilter = ColorFilter.tint(Color.White),
                      modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                    )

                    Text(
                      text = "使用电子邮件继续",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.align(Alignment.Center)
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "已拥有账号？",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                  )
                  Text(
                    text = "登录",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                      .padding(start = 4.dp)
                      .clickable { navigate(ActiveView.LOGIN_OPTIONS, true) }
                  )
                }
              }
            }
          }

          // ==================== 3. LOGIN_OPTIONS (登录选择) ====================
          ActiveView.LOGIN_OPTIONS -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "回归纯粹自然，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "感知土壤温度。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 65.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Button(
                  onClick = { navigate(ActiveView.FORM_LOGIN, true) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Text(
                    text = "使用已有账号继续",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E5F41)
                  )
                }

                Button(
                  onClick = { navigate(ActiveView.EMAIL_LOGIN, true) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(25.dp)),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                      painter = painterResource(id = R.drawable.email),
                      contentDescription = "Email",
                      colorFilter = ColorFilter.tint(Color.White),
                      modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                    )

                    Text(
                      text = "使用电子邮件继续",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.align(Alignment.Center)
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "没有账号？",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                  )
                  Text(
                    text = "立即注册",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                      .padding(start = 4.dp)
                      .clickable { navigate(ActiveView.SIGNUP_OPTIONS, true) }
                  )
                }
              }
            }
          }

          // ==================== 4. FORM_LOGIN (已有用户名密码登录) ====================
          ActiveView.FORM_LOGIN -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "同步植宠空间，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "开始绿色对话。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 65.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(text = "用户名", fontSize = 11.sp, color = Color.White)

                  CustomInputField(
                    value = username,
                    onValueChange = {
                      username = it
                      if (isError) isError = false
                    },
                    isError = isError,
                    isFocused = usernameFocus,
                    onFocusChanged = { usernameFocus = it }
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(text = "密码", fontSize = 11.sp, color = Color.White)

                  CustomPasswordField(
                    value = password,
                    onValueChange = {
                      password = it
                      if (isError) isError = false
                    },
                    isVisible = isPasswordVisible,
                    onToggleVisible = { isPasswordVisible = !isPasswordVisible },
                    isError = isError,
                    isFocused = passwordFocus,
                    onFocusChanged = { passwordFocus = it }
                  )
                }

                Button(
                  onClick = { handleLogin() },
                  enabled = !isLoading,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF1E5F41)
                      )
                      Text("正在接入...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    } else {
                      Text("立即登录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    }
                  }
                }
              }
            }
          }

          // ==================== 5. EMAIL_SIGNUP (邮箱注册页) ====================
          ActiveView.EMAIL_SIGNUP -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "开启青莳，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "守护方寸生机。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 65.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(text = "电子邮箱", fontSize = 11.sp, color = Color.White)
                  CustomInputField(
                    value = email,
                    onValueChange = {
                      email = it
                      if (isError) isError = false
                    },
                    isError = isError,
                    isFocused = emailFocus,
                    onFocusChanged = { emailFocus = it },
                    keyboardType = KeyboardType.Email
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(text = "注册密码", fontSize = 11.sp, color = Color.White)
                  CustomPasswordField(
                    value = emailPassword,
                    onValueChange = {
                      emailPassword = it
                      if (isError) isError = false
                    },
                    isVisible = isEmailPasswordVisible,
                    onToggleVisible = { isEmailPasswordVisible = !isEmailPasswordVisible },
                    isError = isError,
                    isFocused = emailPasswordFocus,
                    onFocusChanged = { emailPasswordFocus = it }
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(text = "确认密码", fontSize = 11.sp, color = Color.White)
                  CustomPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                      confirmPassword = it
                      if (isError) isError = false
                    },
                    isVisible = isConfirmPasswordVisible,
                    onToggleVisible = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                    isError = isError,
                    isFocused = confirmPasswordFocus,
                    onFocusChanged = { confirmPasswordFocus = it }
                  )
                }

                Button(
                  onClick = { handleEmailSignup() },
                  enabled = !isLoading,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF1E5F41)
                      )
                      Text("注册中...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    } else {
                      Text("确认注册并登录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    }
                  }
                }
              }
            }
          }

          // ==================== 6. EMAIL_LOGIN (邮箱登录页) ====================
          ActiveView.EMAIL_LOGIN -> {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Image(
                  painter = painterResource(id = R.drawable.app),
                  contentDescription = "Logo",
                  modifier = Modifier.size(100.dp)
                )

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "开启青莳，",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 6.dp)
                  )
                  Text(
                    text = "守护方寸生机。",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                      .padding(top = 8.dp)
                      .offset(x = 6.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.weight(1f))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 45.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(text = "电子邮箱", fontSize = 11.sp, color = Color.White)
                  CustomInputField(
                    value = email,
                    onValueChange = {
                      email = it
                      if (isError) isError = false
                    },
                    isError = isError,
                    isFocused = emailFocus,
                    onFocusChanged = { emailFocus = it },
                    keyboardType = KeyboardType.Email
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(text = "密码", fontSize = 11.sp, color = Color.White)
                  CustomPasswordField(
                    value = emailPassword,
                    onValueChange = {
                      emailPassword = it
                      if (isError) isError = false
                    },
                    isVisible = isEmailPasswordVisible,
                    onToggleVisible = { isEmailPasswordVisible = !isEmailPasswordVisible },
                    isError = isError,
                    isFocused = emailPasswordFocus,
                    onFocusChanged = { emailPasswordFocus = it }
                  )
                }

                Button(
                  onClick = { handleEmailLogin() },
                  enabled = !isLoading,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                  shape = RoundedCornerShape(25.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF1E5F41)
                      )
                      Text("正在接入...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    } else {
                      Text("确认登录", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5F41))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // ---------- 5. 顶部左侧超平服极简返回键 ----------
    if (activeView != ActiveView.LANDING) {
      Image(
        painter = painterResource(id = R.drawable.left),
        contentDescription = "Back",
        colorFilter = ColorFilter.tint(Color.White),
        modifier = Modifier
          .padding(top = 52.dp, start = 16.dp)
          .size(26.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { handleBack() }
      )
    }
  }
}

// 通用普通文本输入框
@Composable
private fun CustomInputField(
  value: String,
  onValueChange: (String) -> Unit,
  isError: Boolean,
  isFocused: Boolean,
  onFocusChanged: (Boolean) -> Unit,
  keyboardType: KeyboardType = KeyboardType.Text
) {
  val borderColor = if (isFocused) Color.White else Color.Transparent
  val textColor = if (isError) Color(0xFFFF6B6B) else Color.White

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
      .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      textStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp,
        color = textColor
      ),
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { onFocusChanged(it.isFocused) }
    )
  }
}

// 通用密码输入框 (带眼睛切换按钮)
@Composable
private fun CustomPasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  isVisible: Boolean,
  onToggleVisible: () -> Unit,
  isError: Boolean,
  isFocused: Boolean,
  onFocusChanged: (Boolean) -> Unit
) {
  val borderColor = if (isFocused) Color.White else Color.Transparent
  val textColor = if (isError) Color(0xFFFF6B6B) else Color.White

  // 根据当前是否显示密码，动态切换 preview_open / preview_close 图标
  val iconRes = if (isVisible) R.drawable.preview_open else R.drawable.preview_close

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
      .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      textStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp,
        color = textColor
      ),
      visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      modifier = Modifier
        .weight(1f)
        .onFocusChanged { onFocusChanged(it.isFocused) }
    )

    Image(
      painter = painterResource(id = iconRes),
      contentDescription = "Toggle Password Visibility",
      colorFilter = ColorFilter.tint(Color.White),
      modifier = Modifier
        .size(22.dp)
        .clickable { onToggleVisible() }
    )
  }
}