---
name: dochia-fuzz
description: >
  Run continuous random fuzzing against API endpoints using Dochia. Use when the user wants to fuzz APIs,
  discover edge cases through random mutations, run chaos testing, or perform long-running mutation testing
  on specific endpoints. Also use when the user mentions stress testing, finding crashes, hunting for 500s,
  or mutation-based API testing. Requires an OpenAPI spec file and a running server.
metadata:
  triggers: |
    fuzz my API
    find edge cases
    chaos testing
    stress test my endpoint
    hunt for 500 errors
    mutation testing
    run dochia fuzz
    continuous fuzzing
    random API testing
  examples: |
    dochia fuzz -c openapi.yml -s http://localhost:8080 --path /api/users --http-method POST --stop-after-time-in-sec 300 --mc 500
    dochia fuzz -c openapi.yml -s http://localhost:8080 --path /api/orders --http-method PUT --stop-after-errors 10 --mc 500
---

## Overview

`dochia fuzz` executes continuous random fuzzing with mutators rather than structured playbooks. It randomly mutates
requests to discover unexpected vulnerabilities and edge cases.

## Prerequisites

- Dochia CLI installed (any of the following):
    - `brew install dochia-dev/tap/dochia-cli`
    - `curl -sSL get.dochia.dev | sh`
    - `docker pull dochiadev/dochia-cli`
- An OpenAPI specification file (YAML or JSON)
- A running API server to fuzz

## Basic Usage

```bash
dochia fuzz -c <contract> -s <server_url> --path <path> --http-method <method>
```

## Common Options

| Option                           | Description                                       |
|----------------------------------|---------------------------------------------------|
| `-c, --contract`                 | Path to OpenAPI spec file (required)              |
| `-s, --server`                   | Target server URL (required)                      |
| `--path, -p`                     | API path to fuzz (required)                       |
| `-X, --http-method`              | HTTP method to use (POST, PUT, GET, DELETE, etc.) |
| `--stop-after-time-in-sec, --st` | Stop fuzzing after N seconds                      |
| `--stop-after-errors, --se`      | Stop after N errors found                         |
| `--stop-after-mutations, --sm`   | Stop after N mutations                            |
| `--match-response-codes, --mc`   | Only report these response codes (e.g. 500)       |
| `--match-response-size, --ms`    | Match responses of specific size                  |
| `--match-response-words, --mw`   | Match responses with specific word count          |
| `--match-response-regex, --mr`   | Match responses matching a regex                  |
| `--match-input, --mi`            | Also show the input that caused a match           |
| `-H, --header`                   | Add custom header                                 |
| `--max-requests-per-minute`      | Rate limit                                        |
| `--seed`                         | Reproducible randomness                           |
| `-o, --output`                   | Report output directory                           |
| `--output-format`                | HTML_ONLY, HTML_JS, or BUCKETS                    |

## Examples

```bash
# Fuzz POST /api/users for 5 minutes, report 500s
dochia fuzz -c openapi.yml -s http://localhost:8080 \
  --path /api/users --http-method POST \
  --stop-after-time-in-sec 300 \
  --match-response-codes 500

# Fuzz until 10 errors are found
dochia fuzz -c openapi.yml -s http://localhost:8080 \
  --path /api/orders --http-method PUT \
  --stop-after-errors 10 --mc 500

# Fuzz with auth and rate limiting
dochia fuzz -c openapi.yml -s http://localhost:8080 \
  --path /api/users --http-method POST \
  -H "Authorization=Bearer $TOKEN" \
  --max-requests-per-minute 200 \
  --stop-after-time-in-sec 1800

# Show input payloads that caused matches
dochia fuzz -c openapi.yml -s http://localhost:8080 \
  --path /api/users --http-method POST \
  --mc 500 --match-input
```

## Output & Reports

Fuzz results are written to `./dochia-report` (or the directory specified by `--output`).

Each matched mutation produces:

- `TestN.html` — human-readable report
- `TestN.json` — machine-readable result with full request/response details, including the mutated payload

A `dochia-summary-report.json` is also generated with a compact array of all matched results.

### Quick Report Analysis

```bash
# Count matched mutations
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error")] | length'

# Show response codes distribution
cat dochia-report/dochia-summary-report.json | jq '[.testCases[].httpResponseCode] | group_by(.) | map({code: .[0], count: length})'

# Get full details of a specific match
cat dochia-report/Test1.json | jq .
```

## Documentation

Full reference: https://docs.dochia.dev/cli/fuzz
