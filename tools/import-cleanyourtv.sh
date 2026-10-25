#!/usr/bin/env bash
set -euo pipefail
# Run in a clean checkout of swansook-cyber/Vela-tv on develop/vela-tv-launcher.
# Imports the upstream launcher only; never touches a TV or production.
EXPECTED_BRANCH="develop/vela-tv-launcher"
test "$(git branch --show-current)" = "$EXPECTED_BRANCH" || { echo "Wrong branch" >&2; exit 1; }
test -z "$(git status --porcelain)" || { echo "Working tree must be clean" >&2; exit 1; }
test ! -e launcher || { echo "launcher/ already exists; refusing overwrite" >&2; exit 1; }
git remote add cleanyourtv https://github.com/Gathin23/cleanyourtv.git 2>/dev/null || true
git fetch --depth=1 cleanyourtv main
UPSTREAM_SHA="$(git rev-parse FETCH_HEAD)"
echo "Upstream commit: $UPSTREAM_SHA"
# Stage only the launcher and required license. Preserve upstream notices.
git restore --source="$UPSTREAM_SHA" --staged --worktree -- launcher
git restore --source="$UPSTREAM_SHA" --staged --worktree -- LICENSE
echo "Review staged files and license, then commit manually:"
git diff --cached --stat
echo "Do not change HOME packages or disable TCL services."
