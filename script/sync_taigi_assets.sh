#!/usr/bin/env bash
# sync_taigi_assets.sh — 由 rime-phah-taibun 產生拍台文 assets 並同步進 assets/shared.
#
# 產生端 (build_trime_package.py --apk-assets) 會整個重建目標目錄,
# 因此這裡先產到暫存目錄再 rsync 疊進 assets/shared — 上游主題與 luna_pinyin
# 等內建檔案不會被刪掉. 重複執行無副作用.
#
# 成功後寫 taigi-assets.lock (拍台文/rime-liur 來源 commit), 供
# script/check_taigi_sync.sh 偵測拍台文 repo 之後還沒同步的 commit.
set -euo pipefail

PHAH_REPO="${PHAH_REPO:-$HOME/projects/rime-phah-taibun}"
LIUR_REPO="${LIUR_REPO:-$HOME/projects/rime-liur-arch}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# 影響 APK assets 的拍台文路徑 (check_taigi_sync.sh 用同一份清單)
source "$ROOT/script/taigi_sync_paths.sh"

if [ ! -d "$PHAH_REPO/scripts" ]; then
    echo "錯誤: 找不到 rime-phah-taibun ($PHAH_REPO), 用 PHAH_REPO= 指定" >&2
    exit 1
fi

# lock 記的是 commit; 未 commit 的變更不可重現, 拒絕同步.
if [ -n "$(git -C "$PHAH_REPO" status --porcelain -- "${TAIGI_SYNC_PATHS[@]}")" ]; then
    echo "錯誤: 拍台文工作樹有未 commit 變更, 先 commit 再同步:" >&2
    git -C "$PHAH_REPO" status --short -- "${TAIGI_SYNC_PATHS[@]}" >&2
    exit 1
fi

STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"' EXIT

# 非 Debian 系（如 Arch）無 /usr/share/common-licenses — 用 PHAH_COMMON_LICENSES
# 指向含 LGPL-3/GPL-3 的目錄（例：PHAH_COMMON_LICENSES=/tmp/common-licenses）。
EXTRA_ARGS=()
if [ -n "${PHAH_COMMON_LICENSES:-}" ]; then
    EXTRA_ARGS+=(--common-licenses-dir "$PHAH_COMMON_LICENSES")
fi

(cd "$PHAH_REPO" && uv run python scripts/build_trime_package.py --with-liur --apk-assets "$STAGE/shared" "${EXTRA_ARGS[@]}")

rsync -a "$STAGE/shared/" "$ROOT/app/src/main/assets/shared/"
echo "synced: $(find "$ROOT/app/src/main/assets/shared" -type f | wc -l) files in assets/shared"

liur_commit="$(git -C "$LIUR_REPO" rev-parse HEAD 2>/dev/null || echo unknown)"
{
    echo "phah_taibun=$(git -C "$PHAH_REPO" rev-parse HEAD)"
    echo "rime_liur=$liur_commit"
} > "$ROOT/taigi-assets.lock"
echo "locked: $(tr '\n' ' ' < "$ROOT/taigi-assets.lock")"
