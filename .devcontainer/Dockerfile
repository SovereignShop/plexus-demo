FROM ubuntu:22.04

# Ensure non-interactive installations (avoid prompts)
ENV DEBIAN_FRONTEND=noninteractive

# Update the repositories and install required packages
RUN apt-get update && apt-get install -y \
    # Install OpenJDK 17
    openjdk-17-jdk \
    # Install utilities
    && apt-get install -y curl wget unzip \
    # Install libassimp-dev for Manifold and rlwrap `clj` CLI tool.
    && apt-get install -y libassimp-dev rlwrap \
    # Cleanup
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Clojure
RUN curl -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh\
    && chmod +x linux-install.sh\
    && ./linux-install.sh\
    && rm ./linux-install.sh

# Clojure LSP server
RUN curl -sLO https://github.com/clojure-lsp/clojure-lsp/releases/download/2024.03.31-19.10.13/clojure-lsp-native-linux-amd64.zip \
    && unzip clojure-lsp-native-linux-amd64.zip \
    && mv clojure-lsp /usr/local/bin/

# Clojure Kondo for linting: https://github.com/clj-kondo/clj-kondo/blob/master/doc/install.md
RUN curl -sLO https://raw.githubusercontent.com/clj-kondo/clj-kondo/master/script/install-clj-kondo\
    && chmod +x install-clj-kondo\
    && ./install-clj-kondo\
    && rm ./install-clj-kondo
