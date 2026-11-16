# Lin New Project Kotlin AI — 簡報內容大綱 (PPT Outline)

對應簡報檔案：`Lin_New_Project_Kotlin_AI_Presentation.pptx`（共 9 頁，16:9 寬螢幕，已內嵌各功能 UI 截圖）

---

## Slide 1：封面頁
- **主標題**：Lin New Project Kotlin AI
- **副標題**：模組化 Android 開發平台 × AI 輔助優化與五大核心功能解析
- **標籤**：`Kotlin + Java` · `Room AutoMigration` · `Retrofit2 + RxJava` · `MPAndroidChart` · `AI-Assisted`

---

## Slide 2：App 介紹 — 模組化整合與 AI 輔助開發
- **專案定位**
  - 整合過往商業專案常見的 **30 項 Android 功能模組**，打造高複用性的元件測試與開發基底。
  - 採用 **Kotlin + Java 混合架構**，結合 ViewBinding、Navigation Component (SafeArgs)、Room、MMKV 與 Retrofit2。
- **AI 輔助三大核心成果**
  1. **功能優化**：升級 `GPSPositioningMethod` 多來源定位與反向地理編碼、導入 Room `AutoMigration (v1→v5)`、統一 `PopupWindowManager` 彈窗管理。
  2. **BUG 處理**：修復圖表模組進入空白及橫長條圖 `5000px` 硬編碼裁切問題、消除高風險 `!!` 非空斷言與 RxJava 記憶體洩漏。
  3. **功能開發**：快速建置 Segmented Tab 圖表互動明細、GPS 座標複製面板、記事本與單元測試覆蓋。

---

## Slide 3：技術架構與首頁總覽
- **技術堆疊一覽**
  - **語言與編譯**：Kotlin 2.2.10 / Java 17 / AGP 9.1.1 / KSP
  - **UI 與導航**：Jetpack Navigation (SafeArgs) / ViewBinding / Material Components
  - **儲存與快取**：Room 2.6.1 (SQLite) + Tencent MMKV 1.2.14
  - **網路與非同步**：Retrofit 2.9.0 / OkHttp3 / Gson / RxJava 2 & 3
  - **視覺化與硬體**：MPAndroidChart v3.1.0 / Canvas / LocationManager / ZXing / NFC
- **UI 截圖**：`docs/screenshots/ui_home.jpg`（首頁 30 項功能模組總覽列表）

---

## Slide 4：核心功能 1 — Room 本地資料庫
- **技術實作**
  - `UserProfileEntity`、`FavoritesEntity`、`HistoryEntity` 三表關聯與 `Gson TypeConverters`。
  - 支援 **AutoMigration (v1 → v5)**（含 `@RenameColumn` Spec）與 `RxJava2` 非同步存取。
- **功能優點**
  - 編譯期 SQL 語法檢查，降低執行期閃退風險。
  - 版本升級自動遷移並保留舊有使用者資料。
  - 刪除與清空提供二次確認彈窗，並於 `onDestroyView()` 自動釋放訂閱防洩漏。
- **使用場景**
  - 離線會員帳號管理、瀏覽歷史與個人收藏紀錄、需長期穩定升級的本地資料快取。
- **UI 截圖**：`docs/screenshots/ui_room.jpg`

---

## Slide 5：核心功能 2 — 多國語系切換
- **技術實作**
  - `LanguageEnum` 集中管理繁中 (`TW`)、簡中 (`CN`)、英文 (`US`)、日文 (`JP`)、西文 (`ES`)。
  - 透過 `MMKV` 儲存語系偏好，於 `MainActivity.attachBaseContext()` 套用 `LocaleList`，並以 `ActivityCompat.recreate()` 即時刷新。
- **功能優點**
  - **即時生效免重啟 App**，切換過程流暢無閃爍。
  - **MMKV 毫秒級讀寫**，冷啟動即可載入正確語言資源。
  - 語系設定與 UI 解耦，新增語系僅需擴充 Enum 與 `strings.xml`。
- **使用場景**
  - 跨境電商、國際化 SaaS 工具、觀光導覽 App，或需提供App內獨立語系切換的商業專案。
- **UI 截圖**：`docs/screenshots/ui_language.jpg`

---

## Slide 6：核心功能 3 — GPS 即時定位與地址反查
- **技術實作**
  - 整合 `GPS_PROVIDER`、`NETWORK_PROVIDER`、`PASSIVE_PROVIDER` 三重來源。
  - 先讀快取座標再註冊 `LocationListener` 即時更新，以 `getBetterLocation()` 綜合時間與精準度篩選最佳座標，搭配背景執行緒 `Geocoder` 解析街道地址。
- **功能優點**
  - **秒開顯示 + 動態精準修正**，解決傳統快取位置常為 `null` 的問題。
  - 彈窗關閉即呼叫 `stopGPS()` 節省電量，並內建權限與 GPS 開關防呆引導。
  - 支援經緯度、精準度、海拔、速度顯示與「一鍵複製座標」。
- **使用場景**
  - 外勤打卡考勤、巡檢回報、物流位置追蹤、附近服務據點查詢。
- **UI 截圖**：`docs/screenshots/ui_gps.jpg`

---

## Slide 7：核心功能 4 — Web API 網路服務
- **技術實作**
  - 基於 `Retrofit 2 + OkHttp 3 + RxJava 2` 封裝統一請求介面 `WebApi.request()`。
  - 整合 `ScalarsConverterFactory` 與 `GsonConverterFactory`，並自訂 4 種 `DefaultAdapter`（Int / Double / Long / String 容錯轉換）。
- **功能優點**
  - **高度容錯解析**：自動攔截後端回傳空字串 `""` 或 `null`，避免數字型別解析崩潰。
  - **請求前網路檢查**：離線時由 `CommonUtils.isConnected()` 快速攔截，減少無效等待。
  - 統一 `IResponseListener<T>` 回調與超時重試（6s 連線 / 30s 讀寫）。
- **使用場景**
  - 企業 RESTful / ASMX Web Service 串接、裝置 UID 認證同步、跨系統開放資料整合。
- **UI 截圖**：`docs/screenshots/ui_webapi.jpg`

---

## Slide 8：核心功能 5 — 互動式數據圖表
- **技術實作**
  - 結合 `MPAndroidChart` 與自繪 Canvas View，透過頂部 Segmented Tab 切換 **圓環/實心餅圖**、**橫向堆疊長條圖**、**五維/六維雷達圖**。
- **功能優點**
  - 支援扇區點擊高亮顯示項目明細、孔徑切換（實心/圓環）與動畫隨機刷新數據。
  - 經 AI 優化修復預設空白頁與長條圖 `5000px` 硬編碼裁切 BUG，版面自適應且可捲動。
  - 兼顧第三方圖表庫效率與自訂 `Canvas` 繪圖彈性。
- **使用場景**
  - 個人記帳消費分析、企業 KPI 業績達成率儀表板、多維度能力評估雷達圖。
- **UI 截圖**：`docs/screenshots/ui_chart.jpg`

---

## Slide 9：五大核心模組總結與展望
- **模組價值總覽**
  - **Room**：結構安全 × AutoMigration 平滑升級
  - **多國語系**：MMKV 高速持久化 × 免重啟即時切換
  - **GPS**：多來源演算法 × 非同步地址反查與省電管理
  - **Web API**：Gson 容錯轉換 × 標準化非同步網路框架
  - **互動圖表**：三合一視覺化 × 高互動明細與 AI 修復優化
- **展望**：持續透過 AI 輔助重構，逐步導入 Jetpack Compose 與自動化測試，打造更穩健的企業級模組化底座。
