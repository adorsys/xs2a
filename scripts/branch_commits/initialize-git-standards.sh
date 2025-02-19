#!/bin/bash

echo "Initializing git standards..."

# Step 1: Update commit messages and setup signing
echo -e "Setting up commit message standards and signing...\n"
chmod 500 ./scripts/branch_commits/setup-git-signing.sh
./scripts/branch_commits/setup-git-signing.sh

# Step 2: Setup git hooks
echo -e "\nSetting up git hooks..."
chmod 500 ./scripts/branch_commits/setup_hooks.sh
./scripts/branch_commits/setup_hooks.sh

echo -e "\nGit standards initialization complete!"
