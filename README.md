# 长者照护系统 (Senior Care Plus)

这是一个专为长者护理设计的Android应用程序，提供多种功能来辅助长者照护。

## 项目概述

这个应用程序是一个使用Kotlin和Jetpack Compose构建的Android应用，专注于长者照护功能。应用程序提供了多种功能，包括健康监测、位置追踪、提醒设置等，帮助照顾者更好地照顾长者。

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myapplication/
│   │   │               ├── auth/            # 用户认证相关代码
│   │   │               │   └── UserManager.kt # 用户管理器
│   │   │               ├── database/        # 数据库相关代码
│   │   │               │   └── AppDatabase.kt # SQLite数据库助手
│   │   │               ├── data/            # 数据层代码
│   │   │               ├── reminder/        # 提醒功能相关代码
│   │   │               │   ├── ReminderManager.kt       # 提醒管理器
│   │   │               │   ├── ReminderReceiver.kt      # 提醒广播接收器
│   │   │               │   ├── ReminderAlertDialog.kt   # 提醒弹窗界面
│   │   │               │   └── ReminderFullScreenDialog.kt # 全屏提醒对话框
│   │   │               ├── ui/              # UI相关代码
│   │   │               │   ├── components/  # 可重用UI组件
│   │   │               │   ├── screens/     # 各功能屏幕
│   │   │               │   │   ├── HomeScreen.kt        # 主页界面
│   │   │               │   │   ├── MapScreen.kt         # 地图界面
│   │   │               │   │   ├── NotificationScreen.kt # 通知界面
│   │   │               │   │   ├── SettingsScreen.kt    # 设置界面
│   │   │               │   │   ├── TimerScreen.kt       # 定时器界面
│   │   │               │   │   ├── HeartRateScreen.kt   # 心率监测界面
│   │   │               │   │   ├── TemperatureScreen.kt # 温度监测界面
│   │   │               │   │   ├── DiaperScreen.kt      # 尿布监测界面
│   │   │               │   │   ├── ButtonScreen.kt      # 按钮界面
│   │   │               │   │   ├── LoginScreen.kt       # 登录界面
│   │   │               │   │   ├── RegisterScreen.kt    # 注册界面
│   │   │               │   │   ├── ProfileScreen.kt     # 个人资料界面
│   │   │               │   │   ├── ReminderViewModel.kt # 提醒视图模型
│   │   │               │   │   └── EquipmentManagementScreen.kt # 设备管理界面
│   │   │               │   ├── theme/       # 主题相关代码
│   │   │               │   ├── slideshow/   # 幻灯片相关代码
│   │   │               │   ├── gallery/     # 图库相关代码
│   │   │               │   └── home/        # 主页相关代码
│   │   │               ├── MainActivity.kt  # 主活动
│   │   │               └── MyApplication.kt # 应用程序类
│   │   ├── res/              # 资源文件
│   │   └── AndroidManifest.xml # 应用程序清单
│   ├── androidTest/          # Android测试代码
│   └── test/                 # 单元测试代码
├── build.gradle.kts          # 项目构建脚本
└── proguard-rules.pro        # ProGuard规则
```

## 主要功能

### 1. 主页功能
- 提供应用程序所有功能的入口
- 支持多语言（中文和英文）
- 支持深色/浅色主题

### 2. 健康监测功能
- 心率监测 (HeartRateScreen)
- 体温监测 (TemperatureScreen)
  - 支持用户特定的体温数据记录和查看
  - 体温数据存储在SQLite数据库中
  - 按时间范围筛选体温记录
  - 异常体温高亮显示
- 尿布监测 (DiaperScreen)

### 3. 提醒系统
- 完整的提醒管理功能
- 支持设置重复提醒（每周特定日期）
- 提醒通知和弹窗提醒
- 支持贪睡功能
- 提醒数据与用户账号关联

### 4. 位置追踪
- 地图功能 (MapScreen)
- 区域管理功能

### 5. 用户管理系统
- 用户注册和登录功能
- 个人资料页面
- 基于SQLite的用户数据存储
- 用户特定数据隔离

### 6. 其他功能
- 紧急呼叫功能
- 设备管理
- 设置界面，支持语言和主题切换

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM (Model-View-ViewModel)
- **导航**：Compose Navigation
- **后台处理**：AlarmManager, BroadcastReceiver
- **数据存储**：SQLite, SharedPreferences

## 系统需求

- Android API 24+ (Android 7.0 Nougat及以上)
- 编译目标：Android 14 (API 35)

## 权限

应用程序需要以下权限：
- `SCHEDULE_EXACT_ALARM` - 用于设置精确的提醒
- `USE_EXACT_ALARM` - 用于使用精确的闹钟功能
- `RECEIVE_BOOT_COMPLETED` - 用于在设备重启后重新设置提醒
- `VIBRATE` - 用于提醒振动功能

## 更新日志

### v7 (最新版本)
- 添加用户注册和登录系统
- 体温监测数据绑定到用户账号
- 提醒数据与用户账号关联
- SQLite数据库集成
- 登录状态UI自动更新
- 改进的用户体验和界面设计 