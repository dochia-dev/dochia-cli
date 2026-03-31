---
name: dochia-test
description: >
  Run comprehensive API testing against OpenAPI specifications using Dochia. Use when the user wants to
  test APIs, run negative/boundary tests, validate API behavior, check for 5XX errors, run security-focused
  API tests, or verify input validation. Also use when the user mentions chaos testing, fuzzing playbooks,
  API hardening, or contract testing. Requires an OpenAPI spec file and a running server. (1 supporting files)
metadata:
  triggers: |
    test my API
    run API tests
    check for 5XX errors
    negative testing
    boundary testing
    validate my endpoints
    security test my API
    check input validation
    API hardening
    chaos testing
    contract testing
    run dochia
    test endpoints with dependencies
    supply reference data
  examples: |
    dochia test -c openapi.yml -s http://localhost:8080 -b
    dochia test -c openapi.yml -s http://localhost:8080 --playbooks "MalformedJson,BypassAuthentication" -b
    dochia test -c openapi.yml -s http://localhost:8080 --path "/api/users" -b
---

## Overview

`dochia test` is the primary command for executing API tests. It generates and runs 100+ fuzzing playbooks against your
API endpoints based on an OpenAPI specification.

## Prerequisites

- Dochia CLI installed (any of the following):
    - `brew install dochia-dev/tap/dochia-cli`
    - `curl -sSL get.dochia.dev | sh`
    - `docker pull dochiadev/dochia-cli`
- An OpenAPI specification file (YAML or JSON)
- A running API server to test against

## Basic Usage

```bash
# Blackbox mode — only report 5XX server errors
dochia test -c <contract> -s <server_url> -b

# Full test with all response code validation
dochia test -c <contract> -s <server_url>
```

## Common Options

| Option                      | Description                                                  |
|-----------------------------|--------------------------------------------------------------|
| `-c, --contract`            | Path to OpenAPI spec file (required)                         |
| `-s, --server`              | Target server URL (required)                                 |
| `-b, --blackbox`            | Only report 5XX errors (ignore contract mismatches)          |
| `-H, --header`              | Add custom header, e.g. `-H "Authorization=Bearer $TOKEN"`   |
| `--path, -p`                | Comma-separated list of paths to test                        |
| `--skip-path`               | Comma-separated list of paths to skip                        |
| `-X, --http-method`         | Filter by HTTP methods (POST, PUT, GET, DELETE, PATCH, etc.) |
| `--skip-http-method`        | Skip specific HTTP methods                                   |
| `-P, --playbooks`           | Comma-separated list of specific playbooks to run            |
| `--skip-playbooks`          | Comma-separated list of playbooks to skip                    |
| `-t, --tags`                | Filter by OpenAPI tags                                       |
| `--operation-id`            | Filter by operation IDs                                      |
| `--mode`                    | Test mode: ALL, NEGATIVE, POSITIVE                           |
| `--max-requests-per-minute` | Rate limit requests                                          |
| `-o, --output`              | Output directory for reports                                 |
| `--output-format`           | Report format: HTML_ONLY, HTML_JS, BUCKETS                   |
| `-d, --dry-run`             | Show what would be tested without executing                  |
| `--use-examples`            | Use examples from the OpenAPI spec for request bodies        |
| `--config`                  | Load options from a properties file                          |
| `-v`                        | Verbosity level (use -v, -vv, or -vvv)                       |
| `--execution-stats`         | Show execution statistics                                    |
| `--json, -j`                | Output results in JSON format                                |
| `--seed`                    | Set random seed for reproducible tests                       |

## Authentication

```bash
# Header-based auth
dochia test -c openapi.yml -s http://localhost:8080 -H "Authorization=Bearer $TOKEN"

# Basic auth
dochia test -c openapi.yml -s http://localhost:8080 --user "username:password"

# Auth refresh script (for token rotation)
dochia test -c openapi.yml -s http://localhost:8080 --auth-refresh-script ./refresh-token.sh
```

## Filtering Tests

```bash
# Test only specific paths
dochia test -c openapi.yml -s http://localhost:8080 --path "/api/users,/api/orders" -b

# Test only POST and PUT methods
dochia test -c openapi.yml -s http://localhost:8080 --http-method "POST,PUT" -b

# Run only specific playbooks
dochia test -c openapi.yml -s http://localhost:8080 --playbooks "MalformedJson,BypassAuthentication" -b

# Test by OpenAPI tags
dochia test -c openapi.yml -s http://localhost:8080 --tags "users,orders" -b

# Skip deprecated operations
dochia test -c openapi.yml -s http://localhost:8080 --skip-deprecated-operations -b
```

## Response Filtering

```bash
# Ignore specific response codes
dochia test -c openapi.yml -s http://localhost:8080 --ignore-codes "400,422"

# Filter by response size/words/lines
dochia test -c openapi.yml -s http://localhost:8080 --filter-codes "500" --filter-words "10"

# Ignore regex patterns in responses
dochia test -c openapi.yml -s http://localhost:8080 --ignore-regex ".*correlation.*"
```

## CI/CD Integration

```bash
# Rate-limited production-safe testing
dochia test -c openapi.yml -s $STAGING_URL -b --max-requests-per-minute 100 --output ./test-results

# With quality gate (fail if error rate exceeds threshold)
dochia test -c openapi.yml -s $STAGING_URL -b --quality-gate 5

# JSON output for pipeline parsing
dochia test -c openapi.yml -s $STAGING_URL -b --json
```

## Proxy & SSL

```bash
# Through a proxy
dochia test -c openapi.yml -s http://localhost:8080 --proxy "http://proxy:8888"

# With SSL keystore
dochia test -c openapi.yml -s https://localhost:8443 --ssl-keystore keystore.jks --ssl-keystore-password changeit
```

## Output & Reports

Reports are generated in `./dochia-report` by default. Use `--output` to change the directory.

Each test produces:

- `TestN.html` — human-readable report
- `TestN.json` — machine-readable result with full request/response details

A `dochia-summary-report.json` is also generated with a compact array of all test results.

See [the report output reference](references/report-output.md) for the full JSON schemas and `jq` examples for parsing
results programmatically.

### Quick Report Analysis

```bash
# Count errors
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error")] | length'

# List unique failing playbooks
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error") | .playbook] | unique'

# List failing paths
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error") | .path] | unique'

# Get full details of a specific test
cat dochia-report/Test449.json | jq .

# Get test IDs for replay
cat dochia-report/dochia-summary-report.json | jq -r '[.testCases[] | select(.result == "error") | .id] | join(",")'
```

## Stateful Testing Workflow

Dochia is stateless — it does not automatically create or manage resources between requests. When testing APIs
with resource dependencies (e.g. `PUT /pet/{petId}` requires a `petId` from `POST /pet`), you need to
orchestrate the workflow yourself.

### Step 1: Understand the resource hierarchy

Use `dochia list --paths` to inspect the API structure and identify parent/child resource relationships:

```bash
dochia list --paths -c openapi.yml
dochia list --paths -c openapi.yml --path /pet/{petId}
```

Look for patterns like:

- `POST /resource` (creates) → `GET/PUT/DELETE /resource/{id}` (requires ID)
- `POST /resource/{id}/subresource` (requires parent ID)

### Step 2: Create resources first using HappyPath

Run the `HappyPath` playbook on creation endpoints (typically POST) to generate valid resources.
Inspect the response body from the JSON report to extract the resource ID.

> **Note:** The ID field name varies by API. Check the OpenAPI spec or the raw response body to find the correct
> field. For example, for a Pet resource, it might be `.id`, `.petId`, `.idOfPet`, `.petIdentifier`, etc.

```bash
# Run only HappyPath on the creation endpoint
dochia test -c openapi.yml -s http://localhost:8080 --path "/pet" --http-method POST --playbooks HappyPath -o /tmp/setup-report

# First, inspect the full response to find the correct ID field name
cat /tmp/setup-report/Test1.json | jq '.response.body'

# Then extract using the actual field name from your API
cat /tmp/setup-report/Test1.json | jq '.response.body.id'         # adjust field name as needed
```

### Step 3: Supply reference data with -R

Use `-R` to pass fixed field values that Dochia will inject into path parameters and request bodies:

```bash
# Inline reference data (applied to all paths)
dochia test -c openapi.yml -s http://localhost:8080 -R "petId=123" -b

# Per-path reference data via YAML file
dochia test -c openapi.yml -s http://localhost:8080 --reference-data refData.yml -b
```

The reference data YAML file format supports per-path and global (`all`) values:

```yaml
/pet/{petId}:
  petId: "123"
/store/order/{orderId}:
  orderId: "456"
all:
  apiVersion: "v2"
```

### Recommended agent workflow

When testing an API with resource dependencies:

1. Run `dochia list --paths -c openapi.yml` to understand the API structure
2. Identify which endpoints create resources (typically POST without path parameters)
3. Run `dochia test` with `--playbooks HappyPath` on creation endpoints first
4. Extract resource IDs from the JSON response in the report
5. Run the full test suite on dependent endpoints using `-R` to supply the extracted IDs
6. Analyze results and replay failures with `dochia replay`

## Listing Available Playbooks

```bash
dochia list --playbooks
dochia list --playbooks --json
```

## Documentation

Full reference: https://docs.dochia.dev/cli/test
