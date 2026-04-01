<div align="center">

## Dochia

### 🤖 Agent-ready • 🧑 Human-friendly

**Bringing chaos with love.**
</div>

Dochia automatically generates and executes negative and boundary API testing, so you and your AI agents can focus on
building, not writing endless test cases or debugging edge-case failures.

Because nobody wants to debug why their "enterprise-grade" API can't handle a simple 🤷‍♀️

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-25+-blue.svg)](https://openjdk.org)
[![GraalVM](https://img.shields.io/badge/GraalVM-Native-orange.svg)](https://www.graalvm.org)
[![Release](https://img.shields.io/github/v/release/dochia-dev/dochia-cli.svg)](https://github.com/dochia-dev/dochia-cli/releases)
![CI](https://img.shields.io/github/actions/workflow/status/dochia-dev/dochia-cli/main.yml?logo=git&logoColor=white)

## What is Dochia?

Dochia is a CLI tool for automated negative, boundary, and chaos API testing.

It runs 120+ deterministic test playbooks that challenge your APIs with malicious, unusual, and edge-case inputs. It
combines negative, boundary, and chaos testing to reveal weaknesses before they surface in production.

**It's like throwing a tantrum at your API, so your users don't have to.**

**The problem**. Engineers spend too much time writing repetitive test cases, traditional automation mostly covers the
happy path, and coding agents waste tokens reasoning about test cases they could just run.

**The solution**. Point Dochia at your OpenAPI spec, and it handles the rest. No test cases to write.

[![Commits](https://img.shields.io/github/commit-activity/m/dochia-dev/dochia-cli?logo=git&logoColor=white)](https://github.com/dochia-dev/dochia-cli/pulse)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dochia-dev_dochia-cli&metric=alert_status&token=3b850d65b8e085c7f98cb046dcb71d289e83d86d)](https://sonarcloud.io/summary/new_code?id=dochia-dev_dochia-cli)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dochia-dev_dochia-cli&metric=bugs&token=3b850d65b8e085c7f98cb046dcb71d289e83d86d)](https://sonarcloud.io/summary/new_code?id=dochia-dev_dochia-cli)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dochia-dev_dochia-cli&metric=code_smells&token=3b850d65b8e085c7f98cb046dcb71d289e83d86d)](https://sonarcloud.io/summary/new_code?id=dochia-dev_dochia-cli)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dochia-dev_dochia-cli&metric=coverage&token=3b850d65b8e085c7f98cb046dcb71d289e83d86d)](https://sonarcloud.io/summary/new_code?id=dochia-dev_dochia-cli)

## See it in action

![Demo](demo.gif)

## Features

- **Instant setup**: Point it at your OpenAPI spec, no test cases required
- **120+ Test playbooks**: Negative, boundary, and chaos scenarios - deterministic and ready to run
- **Context-aware payloads**: Understands your API structure to generate meaningful edge cases
- **Actionable reports**: Specific fixes, not just failure logs
- **Replay mode**: Replay and investigate specific test scenarios
- **Agent-readable reports**: Structured JSON output so coding agents can read, reason, and act on results directly
- **Agent-ready workflows**: Run `dochia init-skills` to expose Dochia skills to your coding agent so they can test as
  they build

## How It Works

### 1. Reads Your OpenAPI Specs

Parses your OpenAPI/Swagger spec to understand your API structure, parameters, and expected data types.

### 2. Generates Smart Payloads

Creates thousands of context-aware test cases including boundary values, XSS payloads, buffer overflow tests, type
confusion attacks, and authentication bypasses.

### 3. Finds Hidden Issues

Runs predefined playbooks with intelligent payload mutation, then analyzes responses for error patterns you wouldn't
have thought to test.

### 4. Surfaces Issues — For You or Your Agent

Results land in `dochia-summary-report.json` plus individual test files per endpoint, structured so both humans and
AI agents can act on them immediately.

## Dochia in Agentic Workflows

As AI agents write more code, they need something to test it first. Dochia closes the loop:

1. Agent writes new API endpoint
2. Agent runs: `dochia test -c api.yml -s localhost:3000`
3. Dochia produces `dochia-summary-report.json` + individual test `.json` files
4. Agent reads errors, fixes code, re-runs
5. Ship with confidence. Human never touched a test

Without Dochia, agents burn tokens reading specs, reasoning about edge cases, generating payloads,
and interpreting raw responses — often across multiple back-and-forth iterations. Dochia handles
all of that internally. One command, structured results.

Run `dochia init-skills` to expose Dochia skills to your coding agent.

## About the Name

**Dochia is a figure from Romanian folklore.**

According to legend, she climbs the mountains believing winter has passed, only to be caught off guard when the cold
returns. One by one, she sheds her layers, trusting the weather has changed, until it suddenly turns against her.

Your API might seem fine until it faces the unexpected. Dochia tests those hidden conditions before they find your
users.

## Quick Start

### Installation

#### Homebrew (macOS/Linux)

```bash
brew install dochia-dev/tap/dochia-cli
```

#### Curl (Linux/macOS)

```bash
curl -sSL https://get.dochia.dev | sh
```

#### Docker

```bash
docker run --rm -v $(pwd):/workspace dochiadev/dochia-cli test -c /workspace/api.yaml -s http://localhost:8080
```

#### Manual Download

```bash
wget https://github.com/dochia-dev/dochia-cli/releases/latest/dochia_platform_version.tar.gz -O dochia.tar.gz
tar -xzf dochia.tar.gz
chmod +x dochia
sudo mv dochia /usr/local/bin/dochia
rm dochia.tar.gz

# Verify installation
dochia --version
```

#### Agent Setup

```bash
dochia init-skills
```

### Basic Usage

```bash
# Blackbox mode — checks for 5XX status codes only
dochia test -c api.yaml -s http://localhost:8080 -b

# Target specific endpoints
dochia test -c api.yaml -s http://localhost:8080 -b --path "/api/users"

# Pass an auth header from an environment variable
dochia test -c api.yaml -s http://localhost:8080 -b --path "/api/users" -H "Api-Key=$API_KEY"

# Replay a specific test
dochia replay Test120
```

### Agent Workflow

```bash
dochia init-skills

# Your agent can now trigger Dochia with natural commands like:
# → "test my API"
# → "check for 5XX errors"
# → "run boundary testing"
# → "validate my endpoints"
# → "check input validation"
```

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Prerequisites

- **Java 25+**: OpenJDK or Oracle JDK
- **GraalVM**: For native binary compilation
- **Maven**: Build tool (wrapper included)

### Development Setup

```bash
git clone https://github.com/dochia-dev/dochia-cli.git
cd dochia
./mvnw clean compile  # build
./mvnw test           # run tests
java -jar target/dochia.jar test -c api.yaml -s http://localhost:8080  # run from JAR
```

### Building Native Binary

```bash
sdk install java 25.0.1-graalce
sdk use java 25.0.1-graalce
./mvnw clean package -Pnative
./target/dochia-runner --version
```

## Is Dochia free?

Yes, the code in this repo is free and open source under the Apache 2.0 license, and Dochia follows an open core model.
A Pro version with additional features and support is coming soon.

## License

Apache 2.0 — see [LICENSE](LICENSE) for details.

## Links

- **Documentation**: [docs.dochia.dev](https://docs.dochia.dev)
- **Website**: [dochia.dev](https://dochia.dev)
- **Issues**: [GitHub Issues](https://github.com/dochia-dev/dochia-cli/issues)
- **Discussions**: [GitHub Discussions](https://github.com/dochia-dev/dochia-cli/discussions)

---

**Let machines do machine work, humans do human work.**