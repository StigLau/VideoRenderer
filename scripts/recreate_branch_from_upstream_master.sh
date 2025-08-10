#!/usr/bin/env bash
set -euo pipefail

# This script creates a new branch based on upstream/master and cherry-picks
# all non-merge commits from the current branch onto it.
#
# Usage:
#   bash scripts/recreate_branch_from_upstream_master.sh
#
# Notes:
# - Requires an 'upstream' remote with a 'master' branch.
# - Exits if there are uncommitted changes.
# - On conflicts, resolve them and run: git add -A && git cherry-pick --continue
#   To abort the cherry-pick: git cherry-pick --abort

# Ensure we're inside a git repo
git rev-parse --is-inside-work-tree >/dev/null

# Ensure clean working tree
if ! git diff-index --quiet HEAD --; then
  echo "Error: working tree has uncommitted changes. Commit or stash before running."
  exit 1
fi

# Verify upstream/master exists
if ! git ls-remote --exit-code upstream master >/dev/null 2>&1; then
  echo "Error: remote 'upstream' or branch 'master' not found."
  echo "Add upstream with: git remote add upstream <url>"
  exit 1
fi

# Determine current branch
curr_branch=$(git symbolic-ref --quiet --short HEAD || echo "")
if [[ -z "$curr_branch" ]]; then
  echo "Error: not on a branch (detached HEAD)."
  exit 1
fi

# Sanitize current branch name for use in new branch name
sanitized_curr=$(echo "$curr_branch" | tr '/:' '--')
timestamp=$(date +%Y%m%d%H%M%S)
new_branch="port-${sanitized_curr}-onto-upstream-master-${timestamp}"

echo "Fetching all remotes..."
git fetch --all --prune

echo "Creating new branch '${new_branch}' from upstream/master..."
git switch -c "${new_branch}" "upstream/master"

# Compute merge-base
base=$(git merge-base "upstream/master" "${curr_branch}")
if [[ -z "${base}" ]]; then
  echo "Error: could not determine merge-base between upstream/master and ${curr_branch}."
  exit 1
fi

echo "Determining commits to cherry-pick from '${curr_branch}'..."
# List commits unique to current branch, oldest-first, excluding merge commits
mapfile -t commits < <(git rev-list --reverse --no-merges "${base}..${curr_branch}")

if [[ ${#commits[@]} -eq 0 ]]; then
  echo "No unique commits to apply. Branch '${new_branch}' created at upstream/master."
  echo "You can push it with: git push -u origin ${new_branch}"
  exit 0
fi

echo "Cherry-picking ${#commits[@]} commit(s)..."
trap 'echo; echo "Cherry-pick stopped due to conflicts."; echo "Resolve conflicts, then run:"; echo "  git add -A && git cherry-pick --continue"; echo "To abort: git cherry-pick --abort"; echo "When done, push: git push -u origin '"${new_branch}"'";' ERR

for c in "${commits[@]}"; do
  echo "Cherry-picking ${c} ..."
  git cherry-pick -x --keep-redundant-commits "${c}"
done

echo
echo "Success! New branch '${new_branch}' is ready."
echo "Push it with: git push -u origin ${new_branch}"
