<div align="center">

# 🎯 Chis

**Cross-platform Application for Android & HarmonyOS**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![HarmonyOS](https://img.shields.io/badge/Platform-HarmonyOS-000000?style=for-the-badge&logo=huawei&logoColor=white)](https://developer.harmonyos.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![CI](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/chis/ci.yml?branch=main&style=for-the-badge&label=CI)](https://github.com/YOUR_USERNAME/chis/actions)

A unified codebase for building native experiences on both Android and HarmonyOS platforms.

</div>

---

## 📖 项目简介

Chis 是一个跨平台应用程序项目，采用 **Mono-repo（单仓库）** 架构，同时维护 Android 和 HarmonyOS 两个平台的原生代码实现。

### 🏗️ 项目结构

```
chis/
├── android/          # Android 原生项目 (Kotlin/Jetpack)
├── harmony/          # HarmonyOS 原生项目 (ArkTS/ArkUI)
├── .github/          # GitHub Actions CI/CD 配置
├── .gitignore        # 统一的 Git 忽略规则
└── README.md         # 项目文档
```

---

## 🔄 平台功能对比

| 功能特性 | Android | HarmonyOS | 状态 |
|---------|---------|-----------|------|
| 核心功能 | ✅ | ✅ | 已完成 |
| UI 界面 | Jetpack Compose | ArkUI | 已完成 |
| 网络请求 | Retrofit/OkHttp | @ohos.net.http | 已完成 |
| 数据持久化 | Room/SQLite | @ohos.data.relationalStore | 已完成 |
| 状态管理 | ViewModel/StateFlow | @State/@Link | 已完成 |
| 跨平台框架 | ArkUI-X | 原生 | 已完成 |

---

## 🚀 快速开始

### 环境要求

| 平台 | 工具 | 版本要求 |
|------|------|---------|
| Android | Android Studio | Ladybug 或更高版本 |
| Android | JDK | 17+ |
| Android | Gradle | 8.0+ |
| HarmonyOS | DevEco Studio | 5.0 或更高版本 |
| HarmonyOS | Node.js | 18+ |
| HarmonyOS | OHPM | 包含在 DevEco Studio 中 |

---

### 📱 Android 开发

#### 1. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/chis.git
cd chis
```

#### 2. 使用 Android Studio 打开项目

```bash
# 方式一：命令行打开
studio64.exe android/

# 方式二：GUI 操作
# 1. 打开 Android Studio
# 2. 选择 "Open" 
# 3. 导航到项目根目录下的 android/ 文件夹
# 4. 点击 "OK"
```

#### 3. 同步与构建

```bash
# 进入 Android 目录
cd android/

# 同步 Gradle 依赖
./gradlew sync

# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 安装到设备
./gradlew installDebug
```

#### 4. 项目配置

确保 `android/local.properties` 文件中配置了正确的 SDK 路径：

```properties
sdk.dir=/path/to/your/Android/Sdk
```

---

### 🔮 HarmonyOS 开发

#### 1. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/chis.git
cd chis
```

#### 2. 使用 DevEco Studio 打开项目

```bash
# 方式一：命令行打开 (Windows)
devecostudio64.exe harmony/

# 方式二：GUI 操作
# 1. 打开 DevEco Studio
# 2. 选择 "Open Project"
# 3. 导航到项目根目录下的 harmony/ 文件夹
# 4. 点击 "OK"
```

#### 3. 安装依赖与构建

```bash
# 进入 HarmonyOS 目录
cd harmony/

# 安装 OHPM 依赖
ohpm install

# 构建 HAP 包
hvigor assembleHap

# 运行测试
hvigor test

# 安装到设备/模拟器
hvigor install
```

#### 4. 项目配置

确保 `harmony/local.properties` 文件中配置了正确的 SDK 路径：

```properties
# DevEco Studio 会自动生成此文件
# 确保 OHOS_SDK_HOME 路径正确
```

---

## 🛠️ 开发工作流

### 分支策略

```
main (生产分支)
  ├── develop (开发分支)
  │   ├── feature/android-* (Android 功能分支)
  │   └── feature/harmony-* (HarmonyOS 功能分支)
  └── release/* (发布分支)
```

### 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

# 示例
feat(android): 添加用户登录功能
fix(harmony): 修复首页加载问题
docs(readme): 更新开发文档
```

**类型说明：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

---

## 📦 CI/CD

项目使用 GitHub Actions 进行持续集成，配置位于 `.github/workflows/ci.yml`。

### 触发条件

| 触发事件 | Android 构建 | HarmonyOS 构建 |
|---------|-------------|----------------|
| Push to main | ✅ | ✅ |
| Push to develop | ✅ | ✅ |
| PR with android/** changes | ✅ | ❌ |
| PR with harmony/** changes | ❌ | ✅ |

### 构建状态

- Android: ![Android CI](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/chis/ci.yml?branch=main&label=Android)
- HarmonyOS: ![HarmonyOS CI](https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/chis/ci.yml?branch=main&label=HarmonyOS)

---

## 📚 文档

- [Android 开发指南](./android/README.md)
- [HarmonyOS 开发指南](./harmony/README.md)
- [API 文档](./docs/api.md)
- [架构设计](./docs/architecture.md)

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 代码审查要求

- [ ] 代码符合项目规范
- [ ] 单元测试通过
- [ ] 文档已更新
- [ ] 无编译警告

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 团队

- **开发者** - [Your Name](https://github.com/YOUR_USERNAME)

---

## 🙏 致谢

- [Android Developers](https://developer.android.com)
- [HarmonyOS Developer](https://developer.harmonyos.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [ArkUI](https://developer.harmonyos.com/cn/docs/documentation/doc-guides/arkui-overview-0000001133734472)

---

<div align="center">

**[⬆ 回到顶部](#-chis)**

</div>
