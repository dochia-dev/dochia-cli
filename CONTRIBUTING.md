# Contributing to Dochia

First off, thank you for considering contributing to Dochia! 🎉 It's people like you that make Dochia such a great tool
for API testing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Making Changes](#making-changes)
- [Submitting Changes](#submitting-changes)
- [Style Guidelines](#style-guidelines)
- [Community and Communication](#community-and-communication)

## Code of Conduct

This project and everyone participating in it is governed by the [Dochia Code of Conduct](CODE_OF_CONDUCT.md). By
participating, you are expected to uphold this code. Please report unacceptable behavior
to [contact@dochia.dev](mailto:contact@dochia.dev).

## License Agreement

By submitting a contribution, you certify that you have the right to do so and that your work is provided under the
Apache License 2.0. You agree it may be incorporated into both the open-source and commercial editions of Dochia.

## Open Source vs Pro Features

Dochia maintains both an open-source community edition and Dochia Pro.
To sustain development of both, we cannot accept contributions that
implement Pro-tier functionality in the open-source version.

**Before contributing major features**, please:

1. Check if similar functionality exists in Dochia Pro
2. Open an issue to discuss the feature scope
3. Get maintainer approval before starting development

This helps avoid disappointment and wasted effort on both sides.

## Developer Certificate of Origin (DCO)

This project uses DCO instead of a CLA. All commits must be signed-off to
certify you have the right to contribute the code.

### How to sign-off commits:

```bash
git commit -s -m "Your commit message"
```

Or add manually:

```Signed-off-by: Your Name <your.email@example.com>```

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check [existing issues](https://github.com/dochia-dev/dochia-cli/issues) as you
might find that the problem has already been reported. When you create a bug report, please include as many details as
possible:

- **Use a clear and descriptive title** for the issue
- **Describe the exact steps to reproduce the problem** with as much detail as possible
- **Provide the OpenAPI contract** that causes the issue (sanitized if needed)
- **Include your environment details**: OS, Java version, Dochia version
- **Describe the behavior you observed** and explain what behavior you expected to see
- **Include logs and error messages** if available

### Suggesting Features

Feature suggestions are welcome! Please check existing issues first to avoid duplicates. When suggesting features:

- **Use a clear and descriptive title**
- **Provide a detailed description of the suggested feature**
- **Explain why this feature would be useful** to most Dochia users
- **Describe how it should work** with examples if possible
- **Consider the scope** - would this be better as a plugin or core feature?

### Contributing Code

We welcome code contributions! Here are some ways to contribute:

- **Fix bugs** listed in the issues
- **Implement new playbooks**
- **Implement new data generators**
- **Improve documentation**
- **Add support for new OpenAPI features**
- **Optimize performance**
- **Add new output formats**

## Getting Started

### Prerequisites

Before you begin, ensure you have:

- **Java 21+** or higher installed
- **Maven 3.8+** installed
- **Git** installed and configured
- A **GitHub account**

### Development Environment Setup

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/dochia-cli.git
   cd dochia-cli
   ```

3. **Add the upstream repository**:
   ```bash
   git remote add upstream https://github.com/dochia-dev/dochia-cli.git
   ```

4. **Install dependencies and build**:
   ```bash
   ./mvnw clean install
   ```

5. **Package and Run tests** to ensure everything works:
   ```bash
   ./mvnw test package
   ```

6. **Verify the build**:
   ```bash
   java -jar ./target/dochia-runner.jar --version
   ```

## Making Changes

### Before You Start

1. **Create a new branch** for your feature or fix:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-number-description
   ```

2. **Keep your fork synced** with upstream:
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

### Development Guidelines

- **Write tests** for any new functionality
- **Update documentation** if you change behavior
- **Follow the existing code style** (see Style Guidelines below)
- **Make focused commits** - one logical change per commit
- **Write descriptive commit messages**

### Testing Your Changes

- **Run unit tests**: `mvn test` and make sure you don't break existing functionality

## Submitting Changes

### Pull Request Process

1. **Ensure your branch is up to date** with the main branch
2. **Run the full test suite** and ensure it passes
3. **Update the CHANGELOG.md** with a description of your changes
4. **Create a Pull Request** with:
    - **Clear title** describing the change
    - **Detailed description** explaining what and why
    - **Link to related issues** if applicable
    - **Screenshots or examples** if relevant

### Pull Request Requirements

- [ ] **Tests pass** - CI must be green
- [ ] **Code coverage** maintained or improved
- [ ] **Documentation updated** if behavior changes
- [ ] **Follows code style** guidelines
- [ ] **No merge conflicts** with main branch

### Review Process

- **All Pull Requests require review** by at least one maintainer
- **Maintainers may suggest changes** - please address feedback promptly
- **CI must pass** before merging
- **Large changes** may require discussion in an issue first

## Style Guidelines

### Java Code Style

- **Use Google Java Style Guide** as the base
- **Maximum line length**: 120 characters
- **Indentation**: 4 spaces (no tabs)

### Commit Messages

Follow the [Conventional Commits](https://conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Examples:

- `feat(playbook): add support for XML payloads`
- `fix(cli): handle empty OpenAPI specs gracefully`
- `docs(readme): update installation instructions`

### Documentation

- **Use clear, concise language**
- **Include code examples** where helpful
- **Keep documentation up to date** with code changes
- **Use proper Markdown formatting**

## Community and Communication

### Getting Help

- **GitHub Issues**: For bugs and feature requests

### Communication Guidelines

- **Be respectful and inclusive** in all interactions
- **Use clear, descriptive titles** for issues and PRs
- **Provide context** when asking for help
- **Be patient** - maintainers are volunteers with other commitments
- **Search before asking** - your question might already be answered

## Recognition

Contributors are recognized in several ways:

- **Contributors section** in the README.md
- **Release notes** mention significant contributions
- **GitHub contributor graphs** show your contributions
- **Special thanks** for major features or fixes

## Questions?

Don't hesitate to ask! We're here to help:

- **Open an issue** for bugs or feature requests
- **Start a discussion** for questions about contributing
- **Join our Discord** for real-time conversations
- **Email the maintainers** at [contact@dochia.dev](mailto:contact@dochia.dev)

---

Thank you for contributing to Dochia! Your efforts help make API testing better for everyone.