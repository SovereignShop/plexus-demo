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
RUN curl -O https://download.clojure.org/install/linux-install-1.11.1.1413.sh\
    && chmod +x linux-install-1.11.1.1413.sh\
    && ./linux-install-1.11.1.1413.sh\
    && rm ./linux-install-1.11.1.1413.sh

# Clojure LSP server
COPY ./install-latest-clojure-lsp.sh ./
RUN ./install-latest-clojure-lsp.sh\
    && rm ./install-latest-clojure-lsp.sh

# Clojure Kondo for linting: https://github.com/clj-kondo/clj-kondo/blob/master/doc/install.md
RUN curl -sLO https://raw.githubusercontent.com/clj-kondo/clj-kondo/master/script/install-clj-kondo\
    && chmod +x install-clj-kondo\
    && ./install-clj-kondo\
    && rm ./install-clj-kondo

EXPOSE 8080

CMD ["tail", "-f", "/dev/null"]
