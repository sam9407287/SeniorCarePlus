# 長者照護系統 (Senior Care Plus)

這是一個專為長者護理設計的Android應用程式，提供多種功能來輔助長者照護和管理。

## 專案概述

長者照護系統是使用Kotlin和Jetpack Compose構建的Android應用，專注於提供全面的長者照護功能。應用程式提供健康監測、位置追蹤、提醒設置、用戶管理等功能，幫助照顧者和長者獲得更好的照護體驗。

## 專案架構

### 檔案結構

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── seniorcareplus/
│   │   │           └── app/
│   │   │               ├── auth/            # 用戶認證相關代碼
│   │   │               │   └── UserManager.kt # 用戶管理器
│   │   │               ├── database/        # 資料庫相關代碼
│   │   │               │   └── AppDatabase.kt # SQLite資料庫助手
│   │   │               ├── models/          # 資料模型
│   │   │               │   └── UserProfile.kt # 用戶個人資料模型
│   │   │               ├── reminder/        # 提醒功能相關代碼
│   │   │               │   ├── ReminderManager.kt       # 提醒管理器
│   │   │               │   ├── ReminderReceiver.kt      # 提醒廣播接收器
│   │   │               │   ├── ReminderAlertDialog.kt   # 提醒彈窗界面
│   │   │               │   └── ReminderFullScreenDialog.kt # 全屏提醒對話框
│   │   │               ├── ui/              # UI相關代碼
│   │   │               │   ├── components/  # 可重用UI組件
│   │   │               │   │   ├── AbnormalFilterChip.kt  # 異常篩選元件
│   │   │               │   │   └── TimeRangeChip.kt      # 時間範圍選擇元件
│   │   │               │   ├── screens/     # 各功能螢幕
│   │   │               │   │   ├── HomeScreen.kt        # 主頁界面
│   │   │               │   │   ├── MapScreen.kt         # 地圖界面
│   │   │               │   │   ├── NotificationScreen.kt # 通知界面
│   │   │               │   │   ├── SettingsScreen.kt    # 設置界面
│   │   │               │   │   ├── TimerScreen.kt       # 定時器界面
│   │   │               │   │   ├── HeartRateScreen.kt   # 心率監測界面
│   │   │               │   │   ├── TemperatureScreen.kt # 體溫監測界面
│   │   │               │   │   ├── DiaperScreen.kt      # 尿布監測界面
│   │   │               │   │   ├── ButtonScreen.kt      # 緊急按鈕界面
│   │   │               │   │   ├── LoginScreen.kt       # 登入界面
│   │   │               │   │   ├── RegisterScreen.kt    # 註冊界面
│   │   │               │   │   ├── ProfileScreen.kt     # 個人資料界面
│   │   │               │   │   ├── ProfileEditScreen.kt # 個人資料編輯界面
│   │   │               │   │   ├── ReminderViewModel.kt # 提醒視圖模型
│   │   │               │   │   ├── MonitorScreen.kt     # 監控中心界面
│   │   │               │   │   ├── IssueReportScreen.kt # 問題報告界面
│   │   │               │   │   ├── MailboxScreen.kt     # 信箱界面
│   │   │               │   │   ├── AboutUsScreen.kt     # 關於我們界面
│   │   │               │   │   ├── ResidentManagementScreen.kt # 院友管理界面
│   │   │               │   │   ├── StaffManagementScreen.kt # 員工管理界面
│   │   │               │   │   └── EquipmentManagementScreen.kt # 設備管理界面
│   │   │               │   ├── theme/       # 主題相關代碼
│   │   │               │   │   ├── Color.kt             # 顏色定義
│   │   │               │   │   ├── Theme.kt             # 主題設置
│   │   │               │   │   ├── Type.kt              # 字型設置
│   │   │               │   │   └── LanguageManager.kt   # 語言管理器
│   │   │               │   ├── gallery/     # 圖庫相關代碼
│   │   │               │   ├── home/        # 主頁相關代碼
│   │   │               │   └── slideshow/   # 幻燈片相關代碼
│   │   │               ├── utils/           # 工具類
│   │   │               │   └── UserManager.kt # 用戶管理工具
│   │   │               ├── MainActivity.kt  # 主活動
│   │   │               └── MyApplication.kt # 應用程式類
│   │   ├── res/              # 資源文件
│   │   └── AndroidManifest.xml # 應用程式清單
│   ├── androidTest/          # Android測試代碼
│   └── test/                 # 單元測試代碼
├── build.gradle.kts          # 專案構建腳本
└── proguard-rules.pro        # ProGuard規則
```

### 主要模組說明

#### 1. 用戶認證模組 (auth)
- `UserManager.kt`: 處理用戶登入、註冊、登出和個人資料管理
- 支援多種用戶類型：院友、家屬、員工、管理人員和開發人員
- 整合SQLite資料庫和SharedPreferences進行用戶資料持久化

#### 2. 資料庫模組 (database)
- `AppDatabase.kt`: SQLite資料庫助手，管理資料庫操作
- 支援用戶資料、體溫記錄等資料的存儲和查詢
- 提供版本遷移和資料庫升級功能

#### 3. 提醒系統 (reminder)
- `ReminderManager.kt`: 提醒排程管理，與Android系統的AlarmManager整合
- `ReminderReceiver.kt`: 接收系統提醒通知的廣播接收器
- `ReminderAlertDialog.kt` 和 `ReminderFullScreenDialog.kt`: 提醒顯示界面
- 支援每週重複提醒設置和貪睡功能

#### 4. 界面模組 (ui)
- `screens`: 包含所有功能界面，如主頁、監測界面、設置界面等
- `components`: 可重用的UI組件，如異常篩選器、時間範圍選擇器等
- `theme`: 主題和語言設置，支援深色模式和中英文雙語

## 主要功能

### 1. 用戶管理系統
- **登入與註冊**: 支援用戶創建帳號和安全登入
- **個人資料管理**: 提供詳細的個人資料設置，包括姓名、性別、生日、聯絡資訊等
- **多用戶類型**: 支援不同角色的用戶，包括院友、家屬、員工和管理人員
- **資料持久化**: 登入狀態和用戶資訊在應用重啟後保持不變

### 2. 健康監測功能
- **體溫監測**: 記錄和追蹤用戶體溫數據，支援異常數據提醒
  - 圖表顯示體溫變化趨勢
  - 按日期和時間範圍篩選數據
  - 異常體溫高亮顯示
  
- **心率監測**: 記錄和檢視心率數據
  - 圖表顯示心率變化
  - 檢測異常心率並提醒

- **尿布監測**: 追蹤尿布更換時間和狀態
  - 提醒更換尿布
  - 記錄更換歷史

### 3. 提醒系統
- **多功能提醒**: 設置各種類型的提醒，如用藥提醒、檢查提醒等
- **週期性提醒**: 支援每週特定日期的重複提醒設置
- **提醒通知**: 使用系統通知和彈窗來提醒用戶
- **貪睡功能**: 可暫時延遲提醒
- **用戶綁定**: 提醒數據與用戶帳號關聯，確保資料隔離

### 4. 位置追蹤功能
- **地圖顯示**: 整合地圖功能，顯示用戶位置
- **安全區域**: 設置安全區域，離開時發出警報
- **位置歷史**: 記錄位置歷史資料

### 5. 管理功能
- **院友管理**: 管理長者資訊和照護需求
- **員工管理**: 管理護理人員和其他工作人員
- **設備管理**: 追蹤和管理照護設備

### 6. 其他功能
- **緊急呼叫**: 快速求助功能
- **問題報告**: 回報系統問題或提出建議
- **通知系統**: 接收重要通知和消息
- **設置選項**: 自定義應用程式外觀和行為
  - 支援多語言（中文和英文）
  - 深色/淺色主題切換

## 技術實現

### 使用的技術和框架
- **程式語言**: Kotlin
- **UI框架**: Jetpack Compose
- **架構**: MVVM (Model-View-ViewModel)
- **導航**: Compose Navigation
- **資料持久化**: 
  - SQLite 資料庫: 存儲用戶資料、健康記錄等
  - SharedPreferences: 存儲設置和登入狀態
- **後台處理**: 
  - AlarmManager: 處理提醒排程
  - BroadcastReceiver: 接收系統事件和提醒觸發

### 關鍵實現亮點
1. **資料隔離與安全**: 各用戶的資料嚴格隔離，確保隱私和安全
2. **離線優先設計**: 主要功能在無網絡環境下依然可用
3. **自適應界面**: 支援不同尺寸和方向的設備顯示
4. **深色模式**: 完整支援系統深色模式，減輕視覺疲勞
5. **多語言支援**: 完整的中英文雙語界面

## 系統需求

- **Android版本**: API 24+ (Android 7.0 Nougat及以上)
- **編譯目標**: Android 14 (API 35)
- **最低設備要求**: 2GB RAM, 500MB 儲存空間

## 權限需求

應用程式需要以下權限：
- `SCHEDULE_EXACT_ALARM` - 用於設置精確的提醒
- `USE_EXACT_ALARM` - 用於使用精確的鬧鐘功能
- `RECEIVE_BOOT_COMPLETED` - 用於設備重啟後重新設置提醒
- `VIBRATE` - 用於提醒振動功能
- `ACCESS_FINE_LOCATION` - 用於位置追蹤功能（可選）

## 更新日誌

### v8 (最新版本)
- 修復個人資料頁面顯示問題，確保所有資料欄位無論有無值都會顯示
- 修復空白資料欄位在登出後不能保存的問題
- 優化用戶資料更新機制，確保資料一致性
- 改進界面響應性，減少加載時間

### v7
- 添加用戶註冊和登入系統
- 體溫監測數據綁定到用戶帳號
- 提醒數據與用戶帳號關聯
- SQLite資料庫集成
- 登入狀態UI自動更新
- 改進的用戶體驗和界面設計

### v6
- 添加多語言支援（中文和英文）
- 實現深色/淺色主題切換
- 優化提醒系統性能
- 添加緊急呼叫功能
- 更新UI設計，提高可用性

### v5
- 引入地圖和位置追蹤功能
- 新增安全區域設置
- 添加通知中心
- 改進健康監測數據顯示

### v4
- 添加體溫監測功能
- 添加心率監測功能
- 添加尿布監測功能
- 改進提醒系統，支援週期性提醒

## 開發團隊

為台灣長者照護機構開發的專業照護管理系統，旨在提高照護品質和效率。

## 聯絡方式

如有問題或建議，請聯絡開發團隊。 