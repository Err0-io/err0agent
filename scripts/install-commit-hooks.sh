#!/bin/bash
set -e

echo "Installing err0agent commit hooks..."

# Copy commit-msg hook
cp scripts/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
echo "✓ Installed commit-msg hook"

# Configure commit template
git config commit.template .gitmessage
echo "✓ Configured commit message template"

cat << EOF

✅ Commit hooks installed successfully!

The commit-msg hook will validate your commits locally.
To bypass validation: git commit --no-verify (not recommended)

Your commit template is now active. Git will show the template
when you run 'git commit' (without -m flag).

Next steps:
- Read CONTRIBUTING.md for commit message guidelines
- Consider installing IDE extensions (see CONTRIBUTING.md)

EOF
