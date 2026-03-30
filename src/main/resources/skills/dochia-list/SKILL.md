---
name: dochia-list
description: >
  List available Dochia resources including playbooks, mutators, OpenAPI paths, formats, and profiles.
  Use when the user wants to discover what test playbooks are available, inspect OpenAPI contract paths,
  see available mutators and formats, or explore what Dochia can test. Also use before running dochia test
  or dochia fuzz to help the user pick specific playbooks, paths, or methods.
metadata:
  triggers: |
    list dochia playbooks
    what playbooks are available
    show API paths
    list mutators
    what can dochia test
    show available tests
    inspect openapi contract
    list endpoints
  examples: |
    dochia list --playbooks
    dochia list --playbooks --json
    dochia list --mutators
    dochia list --paths -c openapi.yml
---

## Overview

`dochia list` displays available playbooks, mutators, OpenAPI paths, formats, profiles, and fuzzing strategies.

## Basic Usage

```bash
# List all available playbooks
dochia list --playbooks

# List all playbooks in JSON format
dochia list --playbooks --json

# List all mutators
dochia list --mutators

# List all registered profiles
dochia list --profiles

# List supported OpenAPI formats
dochia list --formats

# List paths from an OpenAPI contract
dochia list --paths -c openapi.yml

# List details for a specific path
dochia list --paths -c openapi.yml --path /api/users

# List paths filtered by tag
dochia list --paths -c openapi.yml --tag users
```

## Common Options

| Option            | Description                                  |
|-------------------|----------------------------------------------|
| `-f, --playbooks` | List all registered playbooks                |
| `-m, --mutators`  | List all registered mutators                 |
| `--profiles`      | List all registered profiles                 |
| `--formats`       | List supported OpenAPI formats               |
| `-p, --paths`     | List paths from an OpenAPI contract          |
| `-c, --contract`  | OpenAPI contract file (required for --paths) |
| `--path`          | Show details for a specific path             |
| `--tag`           | Filter paths by OpenAPI tag                  |
| `-j, --json`      | Output in JSON format                        |

## Documentation

Full reference: https://docs.dochia.dev/cli/list
