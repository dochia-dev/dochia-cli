---
name: dochia-explain
description: >
  Get detailed explanations of Dochia playbooks, mutators, response codes, and error reasons. Use when the
  user wants to understand what a specific playbook does, what a 9XX response code means, or get details
  about an error reason from a Dochia test report. Also use after running dochia test or dochia fuzz when
  the user asks about a specific result, error, or response code in the report.
metadata:
  triggers: |
    explain dochia response code
    what does this playbook do
    what does 9XX mean
    explain dochia error
    what is this test result
    explain the error reason
    what does this response code mean
  examples: |
    dochia explain --type response_code 953
    dochia explain --type playbook BypassAuthentication
    dochia explain --type mutator RandomString
    dochia explain --type error_reason "Error details leak"
---

## Overview

`dochia explain` provides detailed information about playbooks, mutators, response codes, and error reasons. This is
useful for understanding test results and debugging failures.

## Basic Usage

```bash
# Explain a 9XX response code
dochia explain --type response_code 953

# Explain a playbook
dochia explain --type playbook BypassAuthentication

# Explain a mutator
dochia explain --type mutator RandomString

# Explain an error reason
dochia explain --type error_reason "Error details leak"
```

## Options

| Option       | Description                                                                |
|--------------|----------------------------------------------------------------------------|
| `-t, --type` | Type to explain: PLAYBOOK, MUTATOR, RESPONSE_CODE, ERROR_REASON (required) |
| `<info>`     | The name, code, or search term to explain (positional argument)            |

## Types

- **PLAYBOOK** — Search and describe test playbooks by name (partial match supported)
- **MUTATOR** — Search and describe mutators by name (partial match supported)
- **RESPONSE_CODE** — Explain Dochia's custom 9XX response codes and standard HTTP codes
- **ERROR_REASON** — Explain error reasons found in test reports (partial match supported)

## Examples

```bash
# Find all playbooks related to "json"
dochia explain --type playbook json

# Understand what response code 999 means
dochia explain --type response_code 999

# Search for error reasons containing "leak"
dochia explain --type error_reason leak
```

## Documentation

Full reference: https://docs.dochia.dev/cli/explain
