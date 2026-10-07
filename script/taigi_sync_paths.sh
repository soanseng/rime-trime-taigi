# shellcheck shell=bash
# 拍台文 repo 中會進 APK assets 的路徑 (build_trime_package.py --apk-assets 的輸入).
# sync_taigi_assets.sh 與 check_taigi_sync.sh 共用.
TAIGI_SYNC_PATHS=(schema lua opencc rime.lua packaging/android scripts/build_trime_package.py)
