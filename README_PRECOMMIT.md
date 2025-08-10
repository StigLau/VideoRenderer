# VideoRenderer - Pre-Commit Sanity Check

## Quick Setup

```bash
# Install pre-commit hooks
./scripts/install-git-hooks.sh
```

## What It Does

Automatically checks staged files before commits using Claude Haiku subagent:

✅ **Blocks commits for critical issues:**
- Merge conflict markers (`<<<<<<< HEAD`)
- Exposed secrets/API keys
- Syntax errors in YAML/JSON
- Security vulnerabilities

⚠️ **Warns about potential issues:**
- Large files (>1MB)
- Long lines (>200 chars)
- Missing newlines

## Daily Usage

```bash
# Normal workflow - checks run automatically
git add .
git commit -m "Add feature"

# If blocked by critical issues:
# 1. Fix the reported issues
# 2. Stage fixes: git add .
# 3. Retry commit

# Emergency bypass (NOT recommended):
git commit --no-verify -m "Hotfix"
```

## Benefits

- **Prevents build failures** from merge conflicts
- **Stops secret leaks** before they reach remote
- **Costs ~$0.01-0.05** per commit with Haiku
- **Saves hours** of debugging failed CI builds

## Documentation

See [docs/development/PRE_COMMIT_SANITY_CHECK.md](docs/development/PRE_COMMIT_SANITY_CHECK.md) for complete setup, customization, and porting to other projects.