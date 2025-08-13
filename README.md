# Dochia

> **Bringing Chaos with Love** - Dochia automatically generates and executes negative and boundary testing so you can
> focus on creative problem-solving. Because nobody wants to debug why their "enterprise-grade" API can't handle a
> simple 🤷‍♀️

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.org)
[![GraalVM](https://img.shields.io/badge/GraalVM-Native-orange.svg)](https://www.graalvm.org)
[![Release](https://img.shields.io/github/v/release/dochia-dev/dochia-cli.svg)](https://github.com/dochia-dev/dochia-cli/releases)

## What is Dochia?

Dochia automatically generates and executes negative and boundary testing so you can
focus on creative problem-solving. It auto-generates malicious, weird, and edge-case inputs to hammer your endpoints. *
*It's like throwing a tantrum at
your API, so your users don't have to.**

## About the Name

Dochia comes from Romanian folklore - a legendary figure known for testing the limits of winter's harshness. Like its
namesake, this tool rigorously tests your APIs to ensure they can withstand any conditions.

Instead of spending 40% of your time writing repetitive test cases and missing critical edge cases, let Dochia do the
heavy lifting while you focus on building amazing features.

## Features

- 🚀 **Instant Setup**: Point it at your OpenAPI spec and go
- 🧠 **Smart Testing**: 100+ generators create realistic, context-aware test cases
- 🔍 **Find Hidden Issues**: Edge cases, invalid inputs, and boundary conditions
- 📊 **Clear Results**: Actionable reports with specific fixes
- 🔄 **Replay Mode**: Replay and investigate specific test scenarios
- 📋 **OpenAPI Native**: Understands your API structure automatically

## Why Dochia?

**The Problem:**

- Engineers spend 40% of time writing repetitive test cases
- Manual testing misses critical edge cases and boundary conditions
- Traditional automation mostly tests the "happy path" scenarios
- These missed edge cases become expensive production failures

**The Solution:**

- Automatically discovers and tests thousands of input variations
- Finds the boundary conditions that would otherwise break production
- 80% less time on manual and automation negative testing
- 95% reduction in "how did that get through testing?" incidents
- More time for creative problem-solving

## Quick Start

### Installation

#### Homebrew (macOS/Linux)

```bash
brew install dochia
```

#### Curl (Linux/macOS)

```bash
curl -sSL https://get.dochia.dev | sh
```

#### Docker

```bash
docker run --rm -v $(pwd):/workspace dochia/cli test -c /workspace/api.yaml -s http://localhost:8080
```

#### Manual Download

```bash
# Download latest release for your platform
wget https://github.com/dochia-dev/dochia-cli/releases/latest/dochia-xxx.tar.gz
tar -xzf dochia-xxx.tar.gz
chmod +x dochia
sudo mv dochia /usr/local/bin/dochia
rm dochia-xxx.tar.gz

# Verify insallation
dochia --version
```

### Basic Usage

```bash
# Test your API using OpenAPI spec in blackbox mode i.e., checking only 500 status codes
dochia test -c api.yaml -s http://locahost:8080 -b -k

# Target specific endpoints
dochia test  -c api.yaml -s http://locahost:8080 -b -k --path "/api/users"

# Pass in an authentication header from the API_KEY environment variable
dochia test  -c api.yaml -s http://locahost:8080 -b -k --path "/api/users" -H "Api-Key=$API_KEY"

# Replay a specific test
dochia replay Test120
```

## How It Works

### 1. **Reads Your OpenAPI Specs**

Dochia automatically parses your OpenAPI/Swagger specifications to understand your API structure, parameters, and
expected data types.

### 2. **Generates Smart Payloads**

Creates thousands of context-aware test cases including:

- Boundary value testing
- XSS payloads
- Buffer overflow tests
- Type confusion attacks
- Authentication bypasses

### 3. **Finds Hidden Issues**

Discovers vulnerabilities and edge cases through:

- Predefined playbooks
- Intelligent payload mutation
- Response analysis
- Error pattern detection

## Documentation

You can find the full documentation at https://docs.dochia.dev.

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/dochia-dev/dochia-cli.git
cd dochia

# Build with Maven
./mvnw clean compile

# Run tests
./mvnw test

# Build native binary with GraalVM
./mvnw clean package -Pnative

# Run from JAR (development)
java -jar target/dochia.jar test -c api.yaml -s http://localhost:8080
```

### Prerequisites for Development

- **Java 21+**: OpenJDK or Oracle JDK
- **GraalVM**: For native binary compilation
- **Maven**: Build tool (wrapper included)

### Building Native Binary

```bash
# Install GraalVM (if not already installed)
sdk install java 22.3.r17-grl
sdk use java 22.3.r17-grl

# Build native executable
./mvnw clean package -Pnative

# Binary will be created at target/dochia
./target/dochia-runner --version
```

## 📄 License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- **Documentation**: [docs.dochia.dev](https://docs.dochia.dev)
- **Website**: [dochia.dev](https://dochia.dev)
- **Issues**: [GitHub Issues](https://github.com/dochia-dev/dochia-cli/issues)
- **Discussions**: [GitHub Discussions](https://github.com/dochia-dev/dochia-cli/discussions)

---

**Let machines do machine work, humans do human work.**
