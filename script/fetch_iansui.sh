#!/usr/bin/env bash
# fetch_iansui.sh — 下載原版芫荽（Iansui）字型進 assets/shared/fonts，作補字字型。
#
# 釘版本＋sha256；檔案原樣（未修改、未切細）散布，附 OFL.txt — 照 SIL OFL 1.1，
# 保留字型名「芫荽」「iansui」只限原版使用。FontManager.typefaceFor 干焦佇
# 系統字型缺字（如 𪜶 U+2A736）的候選才用。升級：改下面三个值、重跑、commit。
set -euo pipefail
export PATH="${PATH:-/usr/bin:/bin}" TMPDIR="${TMPDIR:-/tmp}"

VERSION="v1.020"
ZIP_URL="https://github.com/ButTaiwan/iansui/releases/download/${VERSION}/iansui.zip"
ZIP_SHA256="28d58aef9b7a21c137945a4fd14138d6284a8573c0c1b123f6e5b708423348a6"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/app/src/main/assets/shared/fonts"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

curl -fsSL -o "$WORK/iansui.zip" "$ZIP_URL"
echo "$ZIP_SHA256  $WORK/iansui.zip" | sha256sum -c -
unzip -q "$WORK/iansui.zip" Iansui-Regular.ttf OFL.txt -d "$WORK/x"

mkdir -p "$DEST"
cp -f "$WORK/x/Iansui-Regular.ttf" "$DEST/Iansui-Regular.ttf"
cp -f "$WORK/x/OFL.txt" "$DEST/OFL.txt"
cat > "$DEST/README.txt" <<EOF
芫荽 Iansui ${VERSION} — 原版未修改
來源: ${ZIP_URL}
zip sha256: ${ZIP_SHA256}
授權: SIL Open Font License 1.1（全文見 OFL.txt；保留字型名「芫荽」「iansui」）
著作權: But Ko (https://github.com/ButTaiwan/iansui) / The Klee Project Authors
用途: 寫台文 App 候選列補字 — 干焦系統字型無的漢字（如 𪜶）才用芫荽顯示。
EOF
echo "fetched: $(sha256sum "$DEST/Iansui-Regular.ttf")"
