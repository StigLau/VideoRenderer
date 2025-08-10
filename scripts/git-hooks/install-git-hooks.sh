#!/bin/bash
#
# Install git hooks for VideoRenderer project
# Usage: ./scripts/install-git-hooks.sh
#

set -e

echo "🔗 Installing VideoRenderer git hooks..."

# Create symlinks to our custom hooks
if [ -f scripts/git-hooks/pre-commit ]; then
    ln -sf ../../scripts/git-hooks/pre-commit .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo "✅ Pre-commit hook installed"
else
    echo "❌ scripts/git-hooks/pre-commit not found"
    exit 1
fi

# Make sure Python script is executable
if [ -f scripts/git-hooks/pre-commit-sanity-check.py ]; then
    chmod +x scripts/git-hooks/pre-commit-sanity-check.py
    echo "✅ Sanity check script made executable"
else
    echo "❌ scripts/git-hooks/pre-commit-sanity-check.py not found"
    exit 1
fi

echo ""
echo "🎉 Git hooks installed successfully!"
echo ""
echo "The pre-commit hook will now:"
echo "  - Run Claude Haiku subagent analysis on staged files"
echo "  - Block commits with critical issues (merge conflicts, secrets, etc.)"
echo "  - Show warnings for potential issues"
echo ""
echo "To bypass (NOT recommended): git commit --no-verify"