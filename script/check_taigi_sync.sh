#!/usr/bin/env bash
# check_taigi_sync.sh — 拍台文 repo 自上次 sync_taigi_assets.sh 後, 是否有會影響
# APK assets 的新 commit.
#   exit 0: 已同步
#   exit 1: 有未同步 commit (列出來); 先跑 script/sync_taigi_assets.sh
#   exit 2: 沒有 taigi-assets.lock, 或 lock 的 commit 在拍台文 repo 找不到
set -euo pipefail

PHAH_REPO="${PHAH_REPO:-$HOME/projects/rime-phah-taibun}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOCK="$ROOT/taigi-assets.lock"
source "$ROOT/script/taigi_sync_paths.sh"

if [ ! -f "$LOCK" ]; then
    echo "沒有 taigi-assets.lock — 先跑 script/sync_taigi_assets.sh" >&2
    exit 2
fi

locked="$(sed -n 's/^phah_taibun=//p' "$LOCK")"
if ! git -C "$PHAH_REPO" cat-file -e "$locked^{commit}" 2>/dev/null; then
    echo "lock 記錄的拍台文 commit '$locked' 不在 $PHAH_REPO (用 PHAH_REPO= 指定, 或先 git fetch)" >&2
    exit 2
fi

if git -C "$PHAH_REPO" diff --quiet "$locked" HEAD -- "${TAIGI_SYNC_PATHS[@]}"; then
    echo "已同步: 拍台文 $(git -C "$PHAH_REPO" rev-parse --short "$locked")"
    exit 0
fi

echo "拍台文有尚未同步進 APK 的 commit (lock: $(git -C "$PHAH_REPO" rev-parse --short "$locked")):"
git -C "$PHAH_REPO" log --oneline "$locked..HEAD" -- "${TAIGI_SYNC_PATHS[@]}"
echo "→ 跑 script/sync_taigi_assets.sh 後 commit assets 與 taigi-assets.lock"
exit 1
