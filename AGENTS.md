# AGENTS.md — rime-trime-taigi 維護規則

拍台文 Trime（Phah Tâi-bûn）：[osfans/trime](https://github.com/osfans/trime) 的台語特化分支。
App ID `com.soanseng.phahtaibun`（可與官方同文並存）。網站 <https://taigi.anatomind.com>。

## Remotes 與版本

- `origin` = `soanseng/rime-trime-taigi`（發布用）
- `upstream` = `osfans/trime`（default branch `develop`）
- 我們的版號 `v0.1.x`；上游 base 目前 `v3.3.12`（merge-base `e09ac711`）
- 本 repo 在 GitHub 是 `fork:false`：**沒有**原生「forked from」橫幅，跨 repo compare/PR 已實測 404 — 血緣證明靠 README 聲明＋完整 git 歷史，**勿依賴 GitHub fork network 功能**（compare、跨 repo PR、fork 同步皆不可用）

## 常態工作樹狀態（判讀，勿誤殺）

`app/src/main/jni/librime-lua-deps` 恆帶髒污：

- `M lua5.4/liolib.c`（+2 −1）＝ [`patches/lua.patch`](patches/lua.patch) 已套用 — 修 NDK 舊平台（API <24 且 32 位元）的 `fseeko` 連結。**build 不會自動套用此 patch**，它以 submodule 工作樹髒污形式攜帶。
- `lua5.4/liolib.c.rej`（若有）＝ 重複套用的殘骸。判讀：內容與 lua.patch 同一 hunk、且 `liolib.c` 已含 `ANDROID_PLATFORM` guard（約 118 行）→ 可刪；**內容不同或 guard 不在 → 停下調查，先備份再動**。

任何更新動作前先 `git status` 盤點；**禁止**對 submodule `reset --hard` 或直接覆寫未查明的髒污。

## 上游更新 runbook

1. `git status` — 除上述已知髒污外必須乾淨；有 `.rej` 先按上文判讀處理
2. `git fetch upstream --tags`
3. 挑 **release tag**（`v3.3.x`），**勿併 `develop` 尖端**；先 `git log --oneline HEAD..<tag>` 預覽內容
4. `git merge <tag>` — 用 merge，**禁 rebase**（`main` 已發布、`v0.1.x` tag 存在，改寫歷史會炸 release）
5. 解衝突（熱點見下表）
6. `git submodule update --init --recursive` — 這步**會清掉 lua.patch 髒污**
7. 檢查上游是否已自行修掉：
   `grep -n ANDROID_PLATFORM app/src/main/jni/librime-lua-deps/lua5.4/liolib.c`
   沒修就重套：
   `(cd app/src/main/jni/librime-lua-deps && patch -p1 < ../../../../../patches/lua.patch)`
   套完確認：無新增 `.rej`、`git -C … diff --stat` 為 +2 −1
8. `./script/sync_taigi_assets.sh` — 以 `uv run` 執行（需 [uv](https://docs.astral.sh/uv/)）；需本地 rime-phah-taibun checkout（`PHAH_REPO=` 改路徑；非 Debian 系加 `PHAH_COMMON_LICENSES=`）
   **輸出落在 tracked 的 `app/src/main/assets/shared/` — 未 commit 的變更不會進 tag**
9. `./gradlew assembleDebug` ＋實機煙霧測試（台語／注音／嘸蝦米三方案＋拍台文功能列）
10. **commit 全部**（merge、衝突解法、同步後的 assets）
11. 同步文檔基準：README「分支自 … v3.3.12」字樣與 repo description 改成新 base：
    `gh api -X PATCH repos/soanseng/rime-trime-taigi -f description='…' -f homepage='https://taigi.anatomind.com'`
12. `git tag v0.1.<n> && git push origin main --tags` → GitHub 開 Release（notes 註明對應上游 base）

## 衝突熱點

| 檔案 | 原因 |
|---|---|
| `app/src/main/assets/shared/trime.yaml` | 我們的鍵盤版面 vs 上游主題改動 |
| `app/src/main/res/values*/strings.xml` | `taigi_*` 字串 vs 上游新字串（`values`／`values-zh-rTW`／`values-nan`） |
| `DataManager` | `SCHEMA_LIST_CUSTOM_PATCH`（預裝方案清單） |
| 主題 yaml | 繁體化 diff 遇上游主題更新 |

## 紀律

- 台語資產全部由 `sync_taigi_assets.sh` 從 rime-phah-taibun 產出 — 上游 merge 後**必重跑**，並 commit 其輸出
- tag 前置條件：`git status` 乾淨（僅容忍已知 `liolib.c` 髒污）＋ 實機煙霧通過
- 版本各自獨立：上游 `v3.3.x`、本 repo `v0.1.x`；release notes 記錄對應 base
