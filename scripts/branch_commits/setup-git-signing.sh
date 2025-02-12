#!/bin/bash

# Check if in a git directory
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo "Error: This script must be run inside a Git repository."
    exit 1
fi

# Setup git / ssh for user
echo "********************************************************"
echo "     Configuring Git to use SSH to sign commits "
echo "********************************************************"

read -p "Enter your Adorsys GitLab account username: " username
git config user.name "$username"

read -p "Enter your Adorsys email: " email
git config user.email "$email"

# Store the current directory
current_dir=$(pwd)

# Navigate to $HOME/.ssh
cd $HOME/.ssh || { echo "Failed to navigate to $HOME/.ssh. Exiting."; exit 1; }

# Check if either id_ed25519 or id_rsa exists in $HOME/.ssh
if [[ ! -f $HOME/.ssh/id_ed25519 && ! -f $HOME/.ssh/id_rsa ]]; then
    # Neither id_ed25519 nor id_rsa exists, ask user which key to generate
    echo "No SSH keys found."
    read -p "Do you want to generate an ed25519 key (recommended) or rsa key?
            Type 'ed' for ed25519 or 'rsa' for rsa.
            Press Enter for rsa: " key_choice
    if [[ -z "$key_choice" || "$key_choice" == "rsa" ]]; then
        echo "Generating RSA SSH key."
        ssh-keygen -t rsa -b 4096 -C "$email" -f $HOME/.ssh/id_rsa -N ""
    elif [[ "$key_choice" == "ed" ]]; then
        echo "Generating ed25519 SSH key."
        ssh-keygen -t ed25519 -C "$email" -f $HOME/.ssh/id_ed25519 -N ""
    else
        echo "Invalid input. Defaulting to rsa key generation."
        ssh-keygen -t rsa -b 4096 -C "$email" -f $HOME/.ssh/id_rsa -N ""
    fi
elif [[ -f $HOME/.ssh/id_ed25519 && -f $HOME/.ssh/id_ed25519.pub ]]; then
    echo "Found existing ed25519 SSH key. Using it."
elif [[ -f $HOME/.ssh/id_rsa && -f $HOME/.ssh/id_rsa.pub ]]; then
    echo "Found existing RSA SSH key. Using it."
else
    echo "SSH keys are in an inconsistent state. Please check the existing files."
    exit 1
fi

# Return to the original project directory
cd "$current_dir" || { echo "Failed to return to the project directory. Exiting."; exit 1; }

echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo "Please follow these steps:"
echo "1. Copy the following text output (PUBLIC KEY): "
echo " "
if [[ -f $HOME/.ssh/id_ed25519.pub ]]; then
    cat $HOME/.ssh/id_ed25519.pub
elif [[ -f $HOME/.ssh/id_rsa.pub ]]; then
    cat $HOME/.ssh/id_rsa.pub
fi

echo " "
echo "2. Go to your GitLab account -> Preferences."
echo "2a. Click on SSH Keys."
echo "2b. Click on New SSH key."
echo "2c. In the 'Title' field, add a descriptive label for the new key."
echo "2d. In the 'Key' field, paste your PUBLIC KEY you copied above. Hit 'Add key' button."
echo "If prompted, confirm access to your GitHub/GitLab account."
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"

read -p "Have you completed these steps? (yes/no): " completed
if [[ "$completed" == "yes" || "$completed" == "y" ]]; then
    # Configure Git to use SSH for signing
    git config gpg.format ssh
    if [[ -f $HOME/.ssh/id_ed25519.pub ]]; then
        git config user.signingkey $HOME/.ssh/id_ed25519
    elif [[ -f $HOME/.ssh/id_rsa.pub ]]; then
        git config user.signingkey $HOME/.ssh/id_rsa
    fi
    git config commit.gpgsign true  # Enable signing by default

    # Ensure $HOME/.ssh directory exists (should already exist from earlier in script)
    ssh_dir="$HOME/.ssh"
    mkdir -p "$ssh_dir"

    # Create allowed signers file in .ssh directory
    allowed_signers_file="$ssh_dir/allowed_signers"
    if [[ -f $HOME/.ssh/id_ed25519.pub ]]; then
        echo "$email $(cat $HOME/.ssh/id_ed25519.pub)" > "$allowed_signers_file"
    elif [[ -f $HOME/.ssh/id_rsa.pub ]]; then
        echo "$email $(cat $HOME/.ssh/id_rsa.pub)" > "$allowed_signers_file"
    fi
    chmod 600 "$allowed_signers_file"

    # Configure Git to use the allowed signers file
    git config gpg.ssh.allowedSignersFile "$allowed_signers_file"

    echo "SSH commit signing has been configured successfully!"
    echo "You can now sign commits using:"
    echo "  git commit -S -m 'your commit message'"
    echo "Or just 'git commit -m' since signing is enabled by default"
else
    echo "Run the script again to configure SSH for signing commits."
fi
