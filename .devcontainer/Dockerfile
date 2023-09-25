# Use an official Clojure image as a parent image
FROM ubuntu:22.04

# Ensure non-interactive installations (avoid prompts)
ENV DEBIAN_FRONTEND=noninteractive

# Update the repositories and install required packages
RUN apt-get update && apt-get install -y \
    # Install OpenJDK 17
    openjdk-17-jdk \
    # Install Clojure (use the official script for this)
    && apt-get install -y curl wget unzip \
    # Install libassimp-dev
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

# RUN mkdir -p /root/.deps.clj/1.11.1.1403/ClojureTools && \
#    wget -qO- https://github.com/clojure/brew-install/releases/download/1.11.1.1403/clojure-tools-1.11.1.1403.tar.gz \
#    | tar xvz -C /root/.deps.clj/1.11.1.1403/ClojureTools

# Set the working directory in docker
WORKDIR /workspace

# Copy the current directory contents into the container at /workspace
COPY . /workspace

RUN mkdir -p /var/lib/plexus

# Install system dependencies (if any)

# You can add any other tools or utilities you need

# Expose the port the app runs on (this is optional and depends on your application)
EXPOSE 8080

CMD ["tail", "-f", "/dev/null"]
