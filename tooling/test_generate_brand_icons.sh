#!/bin/sh
# P4.7 self-check: regenerating the brand icon set from the single source mark must reproduce
# byte-identical output (Acceptance: "regenerating from the source reproduces all sizes").
# No framework — just run the generator twice and diff.
set -e

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

swift tooling/generate_brand_icons.swift >/dev/null
cp -R iosApp/MilewayWatch/Assets.xcassets/AppIcon.appiconset "$tmp_dir/appiconset-run1"
cp -R wear/src/main/res/mipmap-mdpi "$tmp_dir/mipmap-run1"

swift tooling/generate_brand_icons.swift >/dev/null
diff -rq iosApp/MilewayWatch/Assets.xcassets/AppIcon.appiconset "$tmp_dir/appiconset-run1"
diff -rq wear/src/main/res/mipmap-mdpi "$tmp_dir/mipmap-run1"

echo "OK: brand icon generation is deterministic across two runs."
