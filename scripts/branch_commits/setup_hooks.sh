#!/bin/bash

# Copy hooks to .git/hooks/
cp hooks/* .git/hooks/
chmod +x .git/hooks/*
echo "Git hooks have been set up."
