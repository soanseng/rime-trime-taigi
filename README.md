<!--
SPDX-FileCopyrightText: 2015 - 2024 Rime community
SPDX-FileCopyrightText: 2026 soanseng

SPDX-License-Identifier: GPL-3.0-or-later
-->

# 寫台文 Trime（舊名：拍台文 Trime）

[![Release](https://img.shields.io/github/v/release/soanseng/rime-trime-taigi.svg)](https://github.com/soanseng/rime-trime-taigi/releases)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

**Android 台語輸入法**，分支（branch）自 [Trime 同文輸入法](https://github.com/osfans/trime) [v3.3.12](https://github.com/osfans/trime/tree/v3.3.12)。本倉庫沿用上游完整 git 歷史，分支點之後的調整全部列在下面〈[我們的調整與設定](#我們的調整與設定相對上游-v3312)〉。

- 🌐 網站：**<https://taigi.anatomind.com>**
- 📖 完整使用說明：<https://taigi.anatomind.com/guide#/>

## 簡介

寫台文是 Trime 的台語特化版：預裝台語（TL/POJ 模糊輸入＋漢羅／全羅）、注音（bopomofo_tw）、嘸蝦米（liur），介面繁體台灣化，鍵盤內建寫台文功能列（選字、翻頁、全羅切換）。應用 ID `com.soanseng.phahtaibun`，**可與官方同文輸入法並存安裝**。

改名說明：本專案與台文雞絲麵製作的「PhahTaigi 台語輸入法」App 無關；因舊名與其過於相近，為免混淆，更名為「寫台文」。

輸入方案與詞典來源：[soanseng/rime-phah-taibun](https://github.com/soanseng/rime-phah-taibun)（MIT）。嘸蝦米檔案集之來源與授權標示見 APK 內 `LIUR-PROVENANCE.txt`、`THIRD-PARTY-NOTICES.txt`。

## 我們的調整與設定（相對上游 v3.3.12）

### 輸入方案與行為

- 預裝三方案：**台語**（TL＋POJ 模糊音，漢羅／全羅輸出）、**注音**（台灣 bopomofo_tw）、**嘸蝦米**（liur）
- 寫台文方案改用 `express_editor`：點選候選字時，涵蓋全部輸入即直接上屏
- 方案清單與預設版面設定集中在 `app/src/main/assets/shared/trime.yaml` 與 `DataManager.SCHEMA_LIST_CUSTOM_PATCH`

### 鍵盤

- 寫台文方案專用 26 鍵版面（無數字列）：q–p 長按＝符號、上滑＝數字 1–0；a 長按＝全選；s 上滑／下滑＝上頁／下頁；z／x／c／v 長按＝剪下／複製／貼上
- 內建**寫台文功能列**：選字、翻頁、羅（TL-POJ）、漢全羅切換
- 切換注音、嘸蝦米等其他方案時＝上游 Trime 原版鍵盤；寫台文鍵盤設定跨主題保留
- 「…」快速面板內建寫台文說明入口；退格鍵面改倒退箭頭

### 主題與介面

- 介面全面**繁體台灣化**；主題雙風格好記名稱、標準主題繁體化
- Settings 語言切換：**繁體中文 ↔ 台文（漢羅）**（`values-nan/`）
- 移除升級通知彈窗；Setup 精靈與標語改「寫台文／tâi-gí」
- 主題切換鍵補 `SWITCH_CHARSET`；面板支援翻頁

### 建置與維護

- 套用上游 `patches/lua.patch`；preBuild 自動產生 `checksums.json`
- [`script/sync_taigi_assets.sh`](script/sync_taigi_assets.sh)：由 rime-phah-taibun 重建並同步台語 assets（不刪上游檔）

## 下載

到 [Releases](https://github.com/soanseng/rime-trime-taigi/releases) 下載最新 APK（v0.1.3+）。

## 從源碼建置

需求：Android SDK＋NDK、JDK 17、[uv](https://docs.astral.sh/uv/getting-started/install/)（同步腳本以 `uv run` 執行 Python；OpenCC 詞典產生在 rime-phah-taibun 的環境內，無需自裝 Python）。

```sh
git clone --recurse-submodules https://github.com/soanseng/rime-trime-taigi.git
cd rime-trime-taigi
./script/sync_taigi_assets.sh   # 需本地 rime-phah-taibun；路徑不同時用 PHAH_REPO= 指定
./gradlew assembleDebug          # Linux/macOS 亦可用 make debug
```

Release 簽署與建置疑難排除同上游：[上游文件](https://github.com/osfans/trime/wiki)、[CONTRIBUTING.md](CONTRIBUTING.md)。

## 從上游 Trime 更新（維護者）

摘要如下；完整 runbook（lua.patch 重套判讀、assets 再同步、tag 前 commit 紀律）見 [AGENTS.md](AGENTS.md)。

```sh
git fetch upstream --tags
git merge v3.3.13                        # 併 release tag；勿併 develop 尖端
git submodule update --init --recursive  # 會清掉 lua.patch — 重套法見 AGENTS.md
./script/sync_taigi_assets.sh           # 重產台語 assets（tracked 檔，須 commit）
./gradlew assembleDebug                  # 實機煙霧 → commit → tag → push
```

## 授權與致謝

- [GPL-3.0-or-later](LICENSE)，沿用上游 [osfans/trime](https://github.com/osfans/trime)（Rime community 2015–2024）及其貢獻者
- 第三方函式庫授權見 [`app/licenses/libraries/`](app/licenses/libraries) 與 APK 內 `THIRD-PARTY-NOTICES.txt`
- 感謝 [RIME](https://rime.im)、[librime](https://github.com/rime/librime) 與上游 Trime 開發社群
