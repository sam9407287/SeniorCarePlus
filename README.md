# 長者照護系統 (Senior Care Plus)

這是一個專為長者護理設計的Android應用程式，提供多種功能來輔助長者照護和管理。

*This is an Android application designed for elderly care, providing various functions to assist in elderly care and management.*

## 專案概述 | Project Overview

長者照護系統是使用Kotlin和Jetpack Compose構建的Android應用，專注於提供全面的長者照護功能。應用程式提供健康監測、位置追蹤、提醒設置、用戶管理等功能，幫助照顧者和長者獲得更好的照護體驗。

*The Senior Care Plus system is an Android application built with Kotlin and Jetpack Compose, focusing on providing comprehensive elderly care functions. The application offers health monitoring, location tracking, reminder settings, user management, and other features to help caregivers and the elderly achieve a better care experience.*

## 專案架構 | Project Architecture

### 檔案結構 | File Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── seniorcareplus/
│   │   │           └── app/
│   │   │               ├── auth/            # 用戶認證相關代碼 | User authentication code
│   │   │               │   └── UserManager.kt # 用戶管理器 | User manager
│   │   │               ├── database/        # 資料庫相關代碼 | Database related code
│   │   │               │   └── AppDatabase.kt # SQLite資料庫助手 | SQLite database helper
│   │   │               ├── models/          # 資料模型 | Data models
│   │   │               │   └── UserProfile.kt # 用戶個人資料模型 | User profile model
│   │   │               ├── reminder/        # 提醒功能相關代碼 | Reminder feature code
│   │   │               │   ├── ReminderManager.kt       # 提醒管理器 | Reminder manager
│   │   │               │   ├── ReminderReceiver.kt      # 提醒廣播接收器 | Reminder broadcast receiver
│   │   │               │   ├── ReminderAlertDialog.kt   # 提醒彈窗界面 | Reminder alert dialog
│   │   │               │   ├── ReminderFullScreenDialog.kt # 全屏提醒對話框 | Full-screen reminder dialog
│   │   │               │   └── ProcessedReminders # 提醒處理系統 | Reminder processing system
│   │   │               ├── ui/              # UI相關代碼 | UI related code
│   │   │               │   ├── components/  # 可重用UI組件 | Reusable UI components
│   │   │               │   │   ├── AbnormalFilterChip.kt  # 異常篩選元件 | Abnormal filter component
│   │   │               │   │   └── TimeRangeChip.kt      # 時間範圍選擇元件 | Time range selection component
│   │   │               │   ├── screens/     # 各功能螢幕 | Feature screens
│   │   │               │   │   ├── HomeScreen.kt        # 主頁界面 | Home screen
│   │   │               │   │   ├── MapScreen.kt         # 地圖界面 | Map screen
│   │   │               │   │   ├── NotificationScreen.kt # 通知界面 | Notification screen
│   │   │               │   │   ├── SettingsScreen.kt    # 設置界面 | Settings screen
│   │   │               │   │   ├── TimerScreen.kt       # 定時器界面 | Timer screen
│   │   │               │   │   ├── HeartRateScreen.kt   # 心率監測界面 | Heart rate monitoring screen
│   │   │               │   │   ├── TemperatureScreen.kt # 體溫監測界面 | Temperature monitoring screen
│   │   │               │   │   ├── DiaperScreen.kt      # 尿布監測界面 | Diaper monitoring screen
│   │   │               │   │   ├── ButtonScreen.kt      # 緊急按鈕界面 | Emergency button screen
│   │   │               │   │   ├── LoginScreen.kt       # 登入界面 | Login screen
│   │   │               │   │   ├── RegisterScreen.kt    # 註冊界面 | Registration screen
│   │   │               │   │   ├── ForgotPasswordScreen.kt # 忘記密碼界面 | Forgot password screen
│   │   │               │   │   ├── VerificationCodeScreen.kt # 驗證碼界面 | Verification code screen
│   │   │               │   │   ├── ResetPasswordScreen.kt # 重設密碼界面 | Reset password screen
│   │   │               │   │   ├── ChangePasswordScreen.kt # 更改密碼界面 | Change password screen
│   │   │               │   │   ├── ProfileScreen.kt     # 個人資料界面 | Profile screen
│   │   │               │   │   ├── ProfileEditScreen.kt # 個人資料編輯界面 | Profile editing screen
│   │   │               │   │   ├── ReminderViewModel.kt # 提醒視圖模型 | Reminder view model
│   │   │               │   │   ├── MonitorScreen.kt     # 監控中心界面 | Monitoring center screen
│   │   │               │   │   ├── IssueReportScreen.kt # 問題報告界面 | Issue reporting screen
│   │   │               │   │   ├── MailboxScreen.kt     # 信箱界面 | Mailbox screen
│   │   │               │   │   ├── AboutUsScreen.kt     # 關於我們界面 | About us screen
│   │   │               │   │   ├── ResidentManagementScreen.kt # 院友管理界面 | Resident management screen
│   │   │               │   │   ├── StaffManagementScreen.kt # 員工管理界面 | Staff management screen
│   │   │               │   │   └── EquipmentManagementScreen.kt # 設備管理界面 | Equipment management screen
│   │   │               │   ├── theme/       # 主題相關代碼 | Theme related code
│   │   │               │   │   ├── Color.kt             # 顏色定義 | Color definitions
│   │   │               │   │   ├── Theme.kt             # 主題設置 | Theme settings
│   │   │               │   │   ├── Type.kt              # 字型設置 | Typography settings
│   │   │               │   │   └── LanguageManager.kt   # 語言管理器 | Language manager
│   │   │               │   ├── gallery/     # 圖庫相關代碼 | Gallery related code
│   │   │               │   ├── home/        # 主頁相關代碼 | Home related code
│   │   │               │   └── slideshow/   # 幻燈片相關代碼 | Slideshow related code
│   │   │               ├── utils/           # 工具類 | Utility classes
│   │   │               │   └── UserManager.kt # 用戶管理工具 | User management utility
│   │   │               ├── MainActivity.kt  # 主活動 | Main activity
│   │   │               │   └── ProcessedReminders # 提醒處理系統 | Reminder processing system
│   │   │               └── MyApplication.kt # 應用程式類 | Application class
│   │   ├── res/              # 資源文件 | Resource files
│   │   └── AndroidManifest.xml # 應用程式清單 | Application manifest
│   ├── androidTest/          # Android測試代碼 | Android test code
│   └── test/                 # 單元測試代碼 | Unit test code
├── build.gradle.kts          # 專案構建腳本 | Project build script
└── proguard-rules.pro        # ProGuard規則 | ProGuard rules
```

### 主要模組說明 | Main Module Description

#### 1. 用戶認證模組 (auth) | User Authentication Module
- `UserManager.kt`: 處理用戶登入、註冊、登出和個人資料管理
  *Handles user login, registration, logout and profile management*
- 支援多種用戶類型：院友、家屬、員工、管理人員和開發人員
  *Supports multiple user types: residents, family members, staff, administrators, and developers*
- 整合SQLite資料庫和SharedPreferences進行用戶資料持久化
  *Integrates SQLite database and SharedPreferences for user data persistence*

#### 2. 資料庫模組 (database) | Database Module
- `AppDatabase.kt`: SQLite資料庫助手，管理資料庫操作
  *SQLite database helper, manages database operations*
- 支援用戶資料、體溫記錄等資料的存儲和查詢
  *Supports storage and querying of user data, temperature records, etc.*
- 提供版本遷移和資料庫升級功能
  *Provides version migration and database upgrade functionality*

#### 3. 提醒系統 (reminder) | Reminder System
- `ReminderManager.kt`: 提醒排程管理，與Android系統的AlarmManager整合
  *Reminder scheduling management, integrated with Android's AlarmManager*
- `ReminderReceiver.kt`: 接收系統提醒通知的廣播接收器
  *Broadcast receiver for system reminder notifications*
- `ReminderAlertDialog.kt` 和 `ReminderFullScreenDialog.kt`: 提醒顯示界面
  *Reminder display interfaces*
- 支援每週重複提醒設置和貪睡功能
  *Supports weekly recurring reminders and snooze functionality*

#### 4. 界面模組 (ui) | Interface Module
- `screens`: 包含所有功能界面，如主頁、監測界面、設置界面等
  *Contains all functional interfaces, such as home page, monitoring interfaces, settings interface, etc.*
- `components`: 可重用的UI組件，如異常篩選器、時間範圍選擇器等
  *Reusable UI components, such as abnormal filters, time range selectors, etc.*
- `theme`: 主題和語言設置，支援深色模式和中英文雙語
  *Theme and language settings, supporting dark mode and bilingual interfaces (Chinese and English)*

## 主要功能 | Main Features

### 1. 用戶管理系統 | User Management System
- **登入與註冊**: 支援用戶創建帳號和安全登入
  *Login and Registration: Supports user account creation and secure login*
- **個人資料管理**: 提供詳細的個人資料設置，包括姓名、性別、生日、聯絡資訊等
  *Profile Management: Provides detailed profile settings, including name, gender, birthday, contact information, etc.*
- **多用戶類型**: 支援不同角色的用戶，包括院友、家屬、員工和管理人員
  *Multiple User Types: Supports different user roles, including residents, family members, staff, and administrators*
- **資料持久化**: 登入狀態和用戶資訊在應用重啟後保持不變
  *Data Persistence: Login status and user information remain unchanged after application restart*

### 2. 健康監測功能 | Health Monitoring Features
- **體溫監測**: 記錄和追蹤用戶體溫數據，支援異常數據提醒
  *Temperature Monitoring: Records and tracks user temperature data, supports abnormal data alerts*
  - 圖表顯示體溫變化趨勢，顯示範圍為34-40°C | *Charts display temperature trends within a 34-40°C range*
  - 將點間連線下方區域使用淺紅色填充，類似心率圖效果 | *Area below connection lines filled with light pink, similar to heart rate chart style*
  - 高溫和低溫閾值以虛線清晰標示，並顯示數值 | *High and low temperature thresholds clearly marked with dashed lines and values*
  - 按日期選擇數據（今日/昨天/前天） | *Filter data by date selection (Today/Yesterday/Day Before)*
  - 異常體溫高亮顯示，使用紅色標記高溫和藍色標記低溫 | *Highlight abnormal temperatures with red for high and blue for low*
  - 支援切換不同病患查看各自的體溫記錄 | *Support switching between different patients to view their temperature records*
  - 體溫記錄可按全部/高溫/低溫類別進行過濾 | *Temperature records can be filtered by All/High/Low categories*
  
- **心率監測**: 記錄和檢視心率數據
  *Heart Rate Monitoring: Records and views heart rate data*
  - 圖表顯示心率變化 | *Charts display heart rate changes*
  - 檢測異常心率並提醒 | *Detect abnormal heart rates and send alerts*

- **尿布監測**: 追蹤尿布更換時間和狀態
  *Diaper Monitoring: Tracks diaper change times and status*
  - 提醒更換尿布 | *Reminds to change diapers*
  - 記錄更換歷史 | *Records change history*

### 3. 提醒系統 | Reminder System
- **多功能提醒**: 設置各種類型的提醒，如用藥提醒、檢查提醒等
  *Multi-functional Reminders: Set various types of reminders, such as medication reminders, check-up reminders, etc.*
- **週期性提醒**: 支援每週特定日期的重複提醒設置
  *Periodic Reminders: Supports recurring reminder settings for specific days of the week*
- **提醒通知**: 使用系統通知和彈窗來提醒用戶
  *Reminder Notifications: Uses system notifications and pop-ups to remind users*
- **貪睡功能**: 可暫時延遲提醒
  *Snooze Function: Can temporarily delay reminders*
- **用戶綁定**: 提醒數據與用戶帳號關聯，確保資料隔離
  *User Binding: Reminder data is associated with user accounts, ensuring data isolation*

### 4. 位置追蹤功能 | Location Tracking Function
- **地圖顯示**: 整合地圖功能，動態顯示用戶位置
  *Map Display: Integrates map functionality, dynamically displays user location*
- **即時追蹤**: 接收MQTT位置數據，實時更新用戶位置
  *Real-time Tracking: Receives MQTT location data, updates user positions in real-time*
- **多用戶追蹤**: 同時追蹤多名長者的位置，採用不同顏色和圖標區分
  *Multi-user Tracking: Simultaneously tracks multiple elderly positions, distinguishes with different colors and icons*
- **客製化移動模式**: 支持不同移動模式的長者顯示（上下移動、左右移動、圓形路徑等）
  *Customized Movement Patterns: Supports display of different movement patterns (vertical, horizontal, circular paths, etc.)*
- **安全區域**: 設置安全區域，離開時發出警報
  *Safety Zones: Sets up safety zones, issues alerts when leaving*
- **位置歷史**: 記錄位置歷史資料
  *Location History: Records historical location data*

### 5. 管理功能 | Management Functions
- **院友管理**: 管理長者資訊和照護需求
  *Resident Management: Manages elderly information and care needs*
- **員工管理**: 管理護理人員和其他工作人員
  *Staff Management: Manages nursing staff and other personnel*
- **設備管理**: 追蹤和管理照護設備
  *Equipment Management: Tracks and manages care equipment*

### 6. 其他功能 | Other Functions
- **緊急呼叫**: 快速求助功能
  *Emergency Call: Quick help function*
- **問題報告**: 回報系統問題或提出建議
  *Issue Reporting: Report system problems or make suggestions*
- **通知系統**: 接收重要通知和消息
  *Notification System: Receives important notices and messages*
- **設置選項**: 自定義應用程式外觀和行為
  *Setting Options: Customize application appearance and behavior*
  - 支援多語言（中文和英文）| *Supports multiple languages (Chinese and English)*
  - 深色/淺色主題切換 | *Dark/light theme switching*

## 更新日誌 | Update Log

### 2025-05-20
- 提升體溫監測界面和功能：
  - 使用與心率圖類似的粉紅色主題增強體溫圖表視覺效果
  - 添加清晰的高溫/低溫閾值虛線，旁邊顯示數值標註
  - 重新組織過濾按鈕位置，移至「體溫記錄」標題下方
  - 改進過濾功能邏輯，確保過濾器只影響記錄列表，不影響圖表顯示
  - 修復界面編譯錯誤並改進整體代碼組織

- 修復病患選擇功能：
  - 實現下拉選單功能，解決不能切換患者的問題
  - 確保選擇新病患後正確顯示對應的體溫數據
  - 當沒有選擇病患時，自動選擇第一個可用病患

*Improved temperature monitoring interface and functionality:*
  - *Enhanced temperature chart with pink theme similar to heart rate chart*
  - *Added clear high/low temperature threshold lines with dashed style and value annotations*
  - *Reorganized filter button placement to below Temperature Records title*
  - *Improved filter logic ensuring filters only affect record list, not the chart*
  - *Fixed UI compilation errors and improved overall code organization*

*Fixed patient selection functionality:*
  - *Implemented dropdown menu functionality to solve patient switching issues*
  - *Ensured correct temperature data display when switching patients*
  - *Automatically selects first available patient when none is selected*

## 技術實現 | Technical Implementation

### 使用的技術和框架 | Technologies and Frameworks Used
- **程式語言**: Kotlin, Python(模擬器)
  *Programming Languages: Kotlin, Python(simulator)*
- **UI框架**: Jetpack Compose
  *UI Framework: Jetpack Compose*
- **架構**: MVVM (Model-View-ViewModel)
  *Architecture: MVVM (Model-View-ViewModel)*
- **通訊協議**: MQTT
  *Communication Protocol: MQTT*
- **JSON處理**: Gson
  *JSON Processing: Gson*
- **靜態類型調整器**: TypeAdapter, TypeAdapterFactory
  *Static Type Adapters: TypeAdapter, TypeAdapterFactory*

### MQTT位置模擬器 | MQTT Location Simulator

本專案包含一個 Python 編寫的 MQTT 位置模擬器，用於測試和演示地圖功能。

*This project includes a Python-based MQTT location simulator for testing and demonstrating the map functionality.*

- **檔案位置**: `/tool/mqtt_location_simulator.py`
  *File Location: `/tool/mqtt_location_simulator.py`*
  
- **功能特點**:
  *Key Features:*
  - 模擬多個長者的位置數據
    *Simulates location data for multiple elderly users*
  - 支援不同的移動模式（上下移動、左右移動、圓形路徑等）
    *Supports various movement patterns (vertical, horizontal, circular paths, etc.)*
  - 使用數學函數產生平滑的運動路徑
    *Uses mathematical functions to generate smooth movement paths*
  - 生成符合系統要求的MQTT JSON格式消息
    *Generates MQTT JSON-formatted messages conforming to system requirements*
  
- **使用方式**:
  *Usage:*
  ```
  cd /Users/sam/Desktop/SeniorCarePlus/tool
  python mqtt_location_simulator.py
  ```

### 對象動態顯示技術 | Dynamic Object Display Technology

地圖上的人物對象實現了以下功能：
*The character objects on the map implement the following features:*

1. **動態加載**: 系統在接收到MQTT位置消息時才加載人物標記
   *Dynamic Loading: The system loads character markers only when receiving MQTT location messages*
   
2. **ID識別與更新**: 使用唯一ID識別人物，確保相同ID只更新位置而不重複創建
   *ID Recognition and Updates: Uses unique IDs to identify characters, ensuring that the same ID only updates position without creating duplicates*
   
3. **座標轉換系統**: 將MQTT消息中的原始座標轉換為地圖顯示座標
   *Coordinate Conversion System: Converts raw coordinates in MQTT messages to map display coordinates*

## 開發工具 | Development Tools
- **導航**: Compose Navigation
  *Navigation: Compose Navigation*
- **資料持久化**: 
  *Data Persistence:*
  - SQLite 資料庫: 存儲用戶資料、健康記錄等
    *SQLite Database: Stores user data, health records, etc.*
  - SharedPreferences: 存儲設置和登入狀態
    *SharedPreferences: Stores settings and login status*
- **後台處理**: 
  *Background Processing:*
  - AlarmManager: 處理提醒排程
    *AlarmManager: Handles reminder scheduling*
  - BroadcastReceiver: 接收系統事件和提醒觸發
    *BroadcastReceiver: Receives system events and reminder triggers*

### 關鍵實現亮點 | Key Implementation Highlights
1. **資料隔離與安全**: 各用戶的資料嚴格隔離，確保隱私和安全
   *Data Isolation and Security: Data for each user is strictly isolated, ensuring privacy and security*
2. **離線優先設計**: 主要功能在無網絡環境下依然可用
   *Offline-First Design: Main functions are still available in offline environments*
3. **自適應界面**: 支援不同尺寸和方向的設備顯示
   *Adaptive Interface: Supports display on devices of different sizes and orientations*
4. **深色模式**: 完整支援系統深色模式，減輕視覺疲勞
   *Dark Mode: Fully supports system dark mode, reducing visual fatigue*
5. **多語言支援**: 完整的中英文雙語界面
   *Multilingual Support: Complete bilingual interface in Chinese and English*

## 系統需求 | System Requirements

- **Android版本**: API 24+ (Android 7.0 Nougat及以上)
  *Android Version: API 24+ (Android 7.0 Nougat and above)*
- **編譯目標**: Android 14 (API 35)
  *Compilation Target: Android 14 (API 35)*
- **最低設備要求**: 2GB RAM, 500MB 儲存空間
  *Minimum Device Requirements: 2GB RAM, 500MB storage space*

## 權限需求 | Permission Requirements

應用程式需要以下權限：
*The application requires the following permissions:*
- `SCHEDULE_EXACT_ALARM` - 用於設置精確的提醒
  *Used for setting precise reminders*
- `USE_EXACT_ALARM` - 用於使用精確的鬧鐘功能
  *Used for using precise alarm functions*
- `RECEIVE_BOOT_COMPLETED` - 用於設備重啟後重新設置提醒
  *Used for resetting reminders after device restart*
- `VIBRATE` - 用於提醒振動功能
  *Used for reminder vibration function*
- `ACCESS_FINE_LOCATION` - 用於位置追蹤功能（可選）
  *Used for location tracking functionality (optional)*

## 更新日誌 | Update Log

### v10 (最新版本 | Latest Version)
- 修復登入相關頁面的UI和導航問題：
  - 更新所有登入相關頁面的標題樣式，使用灰色背景和28sp字體大小
  - 改善UI導航和可訪問性
  - 從所有登入後頁面移除主題切換按鈕，將主題切換功能集中到設置頁面
  - 為登入相關頁面添加設置按鈕，支持語言和主題切換
  - 修復缺少的Language圖標導入問題
  - 設置登入頁面為初始畫面，移除登入頁面的返回按鈕
  - 修復重複導航問題
  - 修改登出功能，使其導航到登入頁面
  - 實現"記住我"功能，提供憑證保存和自動填充
  - 修復登入用戶名顯示問題，確保登入後無需重啟應用即可顯示用戶名
- 修復提醒通知處理問題：
  - 確保提醒類型從通知正確傳遞到應用程式
  - 修復導航，使其返回到之前的畫面
  - 修復提醒通知顯示問題
  - 添加通知權限支持，修復提醒功能
- 添加更改密碼功能和頁面
  *Fixed UI and navigation issues for login-related pages:*
  *- Updated header style for all login-related screens with gray background and 28sp font size*
  *- Improved UI navigation and accessibility*
  *- Removed theme toggle buttons from all post-login pages, centralized theme switching to Settings page only*
  *- Added settings button with language and theme switching to login-related pages*
  *- Fixed missing import for Language icon in RegisterScreen*
  *- Set login page as initial screen, removed back button from login page*
  *- Fixed duplicate navigation issues*
  *- Modified logout to navigate to login page*
  *- Implemented Remember Me functionality for credential saving and auto-filling*
  *- Fixed login username display issue to ensure username appears immediately after login without requiring app restart*
  *Fixed reminder notification handling issues:*
  *- Ensured reminder type is properly passed from notification to app*
  *- Fixed navigation to return to previous screen*
  *- Fixed reminder notification display issues*
  *- Added notification permission support to fix reminder functionality*
  *Added change password feature and page*

### v9
- 添加完整的密碼重置流程功能：
  - 忘記密碼頁面：允許用戶輸入用戶名和電子郵件
  - 驗證碼驗證功能：生成四位數驗證碼並顯示給用戶
  - 重設密碼頁面：允許用戶設置新密碼
  - 支持任何已註冊帳戶進行密碼重設
  - 為測試帳戶添加默認電子郵件地址
  - 增強數據庫和用戶管理器以支持密碼重置流程
  *Added complete password reset flow:*
  *- Forgot password page: allows users to enter username and email*
  *- Verification code feature: generates a four-digit code and displays it to the user*
  *- Reset password page: allows users to set a new password*
  *- Support for password reset for any registered account*
  *- Added default email address for test accounts*
  *- Enhanced database and user manager to support password reset flow*

### v8
- 修復個人資料頁面顯示問題，確保所有資料欄位無論有無值都會顯示
  *Fixed profile page display issue, ensuring all data fields are shown regardless of whether they contain values*
- 將深淺模式切換按鈕從標題欄移至設置頁面，提供更一致的用戶體驗
  *Moved theme toggle button from title bar to settings page for a more consistent user experience*
- 新增修改密碼功能，允許已登入用戶在設置頁面中更新其帳戶密碼
  *Added password change functionality, allowing logged-in users to update their account passwords in the settings page*

### v7
- 添加用戶註冊和登入系統
  *Added user registration and login system*
- 體溫監測數據綁定到用戶帳號
  *Bound temperature monitoring data to user accounts*
- 提醒數據與用戶帳號關聯
  *Associated reminder data with user accounts*
- SQLite資料庫集成
  *Integrated SQLite database*
- 登入狀態UI自動更新
  *Automatically updated login status UI*
- 改進的用戶體驗和界面設計
  *Improved user experience and interface design*

### v6
- 添加多語言支援（中文和英文）
  *Added multilingual support (Chinese and English)*
- 實現深色/淺色主題切換
  *Implemented dark/light theme switching*
- 優化提醒系統性能
  *Optimized reminder system performance*
- 添加緊急呼叫功能
  *Added emergency call functionality*
- 更新UI設計，提高可用性
  *Updated UI design, improved usability*

### v5
- 引入地圖和位置追蹤功能
  *Introduced map and location tracking functionality*
- 新增安全區域設置
  *Added safety zone settings*
- 添加通知中心
  *Added notification center*
- 改進健康監測數據顯示
  *Improved health monitoring data display*

### v4.1
- 改進體溫趨勢圖，調整為34-40°C範圍並實現點間連線下方填充效果
  *Enhanced temperature trend chart with 34-40°C range and area fill below data point connections*
- 優化體溫模擬器，生成更多樣化的溫度數據分布
  *Optimized temperature simulator to generate more diverse temperature data distributions*
- 修復UI中選擇項重複問題
  *Fixed UI issues with duplicate selection elements*

### v4
- 添加體溫監測功能
  *Added temperature monitoring functionality*
- 添加心率監測功能
  *Added heart rate monitoring functionality*
- 添加尿布監測功能
  *Added diaper monitoring functionality*
- 改進提醒系統，支援週期性提醒
  *Improved reminder system, supporting periodic reminders*

## 開發團隊 | Development Team

為台灣長者照護機構開發的專業照護管理系統，旨在提高照護品質和效率。

*A professional care management system developed for Taiwan's elderly care institutions, aimed at improving the quality and efficiency of care.*

## 聯絡方式 | Contact Information

如有問題或建議，請聯絡開發團隊。

*For questions or suggestions, please contact the development team.* 