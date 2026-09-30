#!/usr/bin/env bash
# sync_taigi_assets.sh — 由 rime-phah-taibun 產生拍台文 assets 並同步進 assets/shared.
#
# 產生端 (build_trime_package.py --apk-assets) 會整個重建目標目錄,
# 因此這裡先產到暫存目錄再 rsync 疊進 assets/shared — 上游主題與 luna_pinyin
# 等內建檔案不會被刪掉. 重複執行無副作用.
set -euo pipefail

PHAH_REPO="${PHAH_REPO:-$HOME/projects/rime-phah-taibun}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [ ! -d "$PHAH_REPO/scripts" ]; then
    echo "錯誤: 找不到 rime-phah-taibun ($PHAH_REPO), 用 PHAH_REPO= 指定" >&2
    exit 1
fi

STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"' EXIT

(cd "$PHAH_REPO" && uv run python scripts/build_trime_package.py --with-liur --apk-assets "$STAGE/shared")

rsync -a "$STAGE/shared/" "$ROOT/app/src/main/assets/shared/"
echo "synced: $(find "$ROOT/app/src/main/assets/shared" -type f | wc -l) files in assets/shared"
