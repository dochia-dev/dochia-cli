---
name: dochia-replay
description: >
  Replay previous Dochia test runs to reproduce errors, re-validate fixes, or debug specific test failures.
  Use when the user wants to replay API tests, reproduce errors from a previous run, verify that a bug fix
  resolves previously failing tests, or re-run specific test cases. Also use after running dochia test or
  dochia fuzz when the user wants to confirm fixes or isolate flaky failures.
metadata:
  triggers: |
    replay failing tests
    reproduce the error
    verify the fix
    re-run failed tests
    replay dochia tests
    confirm the bug is fixed
    run the failing test again
  examples: |
    dochia replay --errors -s http://localhost:8080
    dochia replay Test1,Test5 -s http://localhost:8080
    dochia replay --errors -s http://localhost:8080 -r ./previous-report
---

## Overview

`dochia replay` re-executes specific test scenarios from previous test runs. This is useful for reproducing errors,
debugging failures, and validating fixes.

## Prerequisites

- Dochia CLI installed (any of the following):
    - `brew install dochia-dev/tap/dochia-cli`
    - `curl -sSL get.dochia.dev | sh`
    - `docker pull dochiadev/dochia-cli`
- A previous test run with reports in `./dochia-report` (or custom output directory)
- A running API server

## Basic Usage

```bash
# Replay all errors from the last run
dochia replay --errors -s <server_url>

# Replay all warnings from the last run
dochia replay --warnings -s <server_url>

# Replay specific tests by name using the same server url
dochia replay Test1,Test5 

# Replay specific tests by name using a different server url
dochia replay Test1,Test5 -s <server_url>
```

## Common Options

| Option                  | Description                                       |
|-------------------------|---------------------------------------------------|
| `-s, --server`          | Target server URL                                 |
| `--errors`              | Replay all errors from previous run               |
| `--warnings`            | Replay all warnings from previous run             |
| `-v, --verbose`         | Verbose output                                    |
| `-H`                    | Add custom headers                                |
| `-o, --output`          | Output directory                                  |
| `-r, --report-folder`   | Folder containing previous reports to replay from |
| `--proxy`               | Proxy URL                                         |
| `--ssl-keystore`        | SSL keystore for HTTPS                            |
| `--auth-refresh-script` | Script for token refresh                          |

## Examples

```bash
# Replay all errors with verbose output
dochia replay --errors -s http://localhost:8080 -v

# Replay from a specific report folder
dochia replay --errors -s http://localhost:8080 -r ./previous-report

# Replay specific named tests
dochia replay "RemoveFieldPlaybook_POST_/api/users_name,MalformedJsonPlaybook_POST_/api/users" \
  -s http://localhost:8080 -v
```

## Documentation

Full reference: https://docs.dochia.dev/cli/replay
