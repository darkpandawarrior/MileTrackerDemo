#!/usr/bin/env bash
# Installs this repo's local git hooks. Run once after cloning, or whenever
# .git/hooks gets reset. Idempotent.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$(git rev-parse --git-path hooks)"

install_hook() {
  local name="$1" src="$2"
  cp "$src" "$HOOKS_DIR/$name"
  chmod +x "$HOOKS_DIR/$name"
  echo "installed $name"
}

install_hook "pre-commit" "$REPO_ROOT/scripts/no-reference-leak.sh"
