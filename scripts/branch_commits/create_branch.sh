#!/bin/bash

# Usage: ./create_branch.sh <base_branch> <branch_type> <issue_number> <branch_desc>
# Example: ./create_branch.sh main feature 1.1 "basic-html-parser"
# Usage: git create-branch <base_branch> <branch_type> <issue_number> <branch_desc>
# Example: git create-branch develop setup 1923 branching-strategy
# Example: git create-branch setup/issue-1923-branching-strategy ci 1923 branching-strategy

BASE_BRANCH=$1
BRANCH_TYPE=$2
ISSUE_NUMBER=$3
BRANCH_DESC=$4

# Function to validate branch name format
check_branch_name() {
  local branch_name="$1"

  # Check for "master" and "master-<even_number>.x" branches
  if [[ "$branch_name" =~ ^master$ || "$branch_name" =~ ^master-([0-9]+)\.x$ && ${BASH_REMATCH[1]}%2 == 0 ]]; then
    return 0
  fi

  # Check for "v2master" and "v2master-<even_number>.x" branches
  if [[ "$branch_name" =~ ^v2master$ || "$branch_name" =~ ^v2master-([0-9]+)\.x$ && ${BASH_REMATCH[1]}%2 == 0 ]]; then
    return 0
  fi

  # Check for "develop" and "v2develop" branches
  if [[ "$branch_name" == "develop" || "$branch_name" == "v2develop" ]]; then
    return 0
  fi

  # Check for "support" and "support-<even_number>.x" branches
  if [[ "$branch_name" =~ ^support$ || "$branch_name" =~ ^support-([0-9]+)\.x$ && ${BASH_REMATCH[1]}%2 == 0 ]]; then
    return 0
  fi

  # Check for "v2support" and "v2support-<even_number>.x" branches
  if [[ "$branch_name" =~ ^v2support$ || "$branch_name" =~ ^v2support-([0-9]+)\.x$ && ${BASH_REMATCH[1]}%2 == 0 ]]; then
    return 0
  fi

  # Check for "release-x.x" or "release-x.x.x" format
  if [[ "$branch_name" =~ ^release-([0-9]+(\.[0-9]+){1,2})$ ]]; then
    return 0
  fi

  # Check for "v2release-x.x" or "v2release-x.x.x" format
  if [[ "$branch_name" =~ ^v2release-([0-9]+(\.[0-9]+){1,2})$ ]]; then
    return 0
  fi

  # Check for "v2hotfix-x.x.x" format
  if [[ "$branch_name" =~ ^v2hotfix-([0-9]+\.[0-9]+\.[0-9]+)$ ]]; then
    return 0
  fi

  # Check for "v2 develop subbranches" format
  if [[ "$branch_name" =~ ^v2/(feature|setup|bugfix|infra|ci|test|docs|deploy|security|perf|refactor|upgrade)/issue-([0-9]+)-[a-zA-Z0-9_-]+$ ]]; then
    return 0
  fi

  # Check for "v2 support subbranches" format
  if [[ "$branch_name" =~ ^v2/PASD/[0-9]+/issue-([0-9]+)-[a-zA-Z0-9_-]+$ ]]; then
    return 0
  fi

  # Check for "v1 develop subbranches" format
  if [[ "$branch_name" =~ ^(feature|setup|bugfix|infra|ci|test|docs|deploy|security|perf|refactor|upgrade)/issue-([0-9]+)-[a-zA-Z0-9_-]+$ ]]; then
    return 0
  fi

  # Check for "v1 support subbranches" format
  if [[ "$branch_name" =~ ^PASD/[0-9]+/issue-([0-9]+)-[a-zA-Z0-9_-]+$ ]]; then
    return 0
  fi

  # If none of the conditions matched, branch name format is incorrect
  echo "ERROR: Branch name '$branch_name' format is INCORRECT." >&2
  echo "Please use one of the following formats:" >&2
  echo "  master" >&2
  echo "  v2master" >&2
  echo "  develop" >&2
  echo "  v2develop" >&2
  echo "  support or support-<even_number>.x" >&2
  echo "  v2support or v2support-<even_number>.x" >&2
  echo "  release-<x.x> or release-<x.x.x> (where x,y are digits)" >&2
  echo "  v2release-<x.x> or v2release-<x.x.x>" >&2
  echo "  v2hotfix-<x.x.x> (where x are digits)" >&2
  echo "  v2/(feature|setup|bugfix|infra|ci|test|docs|deploy|security|perf|refactor|upgrade)/issue-<gitlab_number>-<description_text>" >&2
  echo "  v2/PASD/<serviceDesk_number>/issue-<gitlab_number>-<description_text>" >&2
  echo "Examples:" >&2
  echo "  master" >&2
  echo "  v2master" >&2
  echo "  support-2.x" >&2
  echo "  v2support-2.x" >&2
  echo "  release-22.0" >&2
  echo "  v2release-22.0" >&2
  echo "  v2hotfix-22.0.1" >&2
  echo "  v2/feature/issue-1234-new-feature" >&2
  echo "  v2/PASD/123/issue-5678-new-feature" >&2
  return 1
}

# Validate input parameters
if [ -z "$BASE_BRANCH" ] || [ -z "$BRANCH_TYPE" ] || [ -z "$ISSUE_NUMBER" ] || [ -z "$BRANCH_DESC" ]; then
  echo "Usage: ./create_branch.sh <base_branch> <branch_type> <issue_number> <branch_desc>"
  echo "Branch type options: feature, setup, bugfix, infra, ci, test, docs, deploy, security, perf, refactor, upgrade"
  echo "For release/hotfix/support branches, use different format:"
  echo "  release-x.x or release-y.y.y"
  echo "  hotfix-y.y.y"
  echo "  support-z.x (where z is even)"
  exit 1
fi

# Validate branch type
if ! [[ "$BRANCH_TYPE" =~ ^(feature|setup|bugfix|infra|ci|test|docs|deploy|security|perf|refactor|upgrade)$ ]]; then
  echo "ERROR: INVALID branch type '$BRANCH_TYPE'."
  echo "Choose from: feature, setup, bugfix, infra, ci, test, docs, deploy, security, perf, refactor, upgrade"
  echo "For release/hotfix/support branches, use the full branch name format directly."
  exit 1
fi

# Construct the branch name
BRANCH_NAME="$BRANCH_TYPE/issue-$ISSUE_NUMBER-$BRANCH_DESC"

# Check if the specified base branch exists
if git show-ref --quiet refs/heads/$BASE_BRANCH; then
  echo "Base branch '$BASE_BRANCH' exists. Checking out..."
else
  echo "ERROR: Specified base branch '$BASE_BRANCH' does not exist." >&2
  exit 1
fi

# Switch to the base branch and update it
git checkout $BASE_BRANCH
git pull origin $BASE_BRANCH

# Validate branch name format
if ! check_branch_name "$BRANCH_NAME"; then
  exit 1
fi

# Create the new branch
git checkout -b "$BRANCH_NAME"

echo "Branch '$BRANCH_NAME' created from '$BASE_BRANCH'."
