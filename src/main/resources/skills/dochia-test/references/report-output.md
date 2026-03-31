# Dochia Report Output Reference

After a `dochia test` or `dochia fuzz` run, reports are written to `./dochia-report/` (or the directory specified by
`--output`).

## Report Directory Structure

```
dochia-report/
├── Test1.html               # Human-readable test report
├── Test1.json               # Machine-readable test result (per test)
├── Test2.html
├── Test2.json
├── ...
├── dochia-summary-report.json  # Summary of all test results
└── index.html                  # HTML overview report
```

## Individual Test Result JSON

Each `TestN.json` file contains the full details of a single test case:

```json
{
  "testId": "Test 449",
  "traceId": "7515d0a5-5dea-46bf-bb78-bd9fe4641b80",
  "scenario": "Send [zero-width characters] in request fields: field [id], value [INSERT with \\u202e], is required [TRUE]",
  "expectedResult": "Should return [2XX]",
  "result": "error",
  "resultReason": "Unexpected behaviour 953",
  "resultDetails": "Unexpected behaviour: expected [200, 201, 202, 204], actual [953]",
  "resultIgnoreDetails": null,
  "request": {
    "headers": [
      {
        "key": "Accept",
        "value": "application/json"
      },
      {
        "key": "Content-Type",
        "value": "application/json"
      },
      {
        "key": "User-Agent",
        "value": "dochia/1.3.1 (Test 449 - ZeroWidthCharsInValuesFields)"
      },
      {
        "key": "X-Dochia-Trace-Id",
        "value": "7515d0a5-5dea-46bf-bb78-bd9fe4641b80"
      }
    ],
    "payload": "{\"id\":\"BKFRCGKZDS\\u202EWXNMWPHFTC\"}",
    "httpMethod": "GET",
    "url": "http://localhost:8080/Brand/BKFRCGKZDS%E2%80%AEWXNMWPHFTC",
    "timestamp": "Sat, 28 Mar 2026 20:46:46 +0200"
  },
  "response": {
    "responseCode": 953,
    "httpMethod": "GET",
    "responseTimeInMs": "5",
    "numberOfWordsInResponse": "0",
    "numberOfLinesInResponse": "0",
    "contentLengthInBytes": "0",
    "jsonBody": {
      ...
    },
    "headers": null,
    "responseContentType": null
  },
  "path": "/Brand/{id}",
  "playbook": "ZeroWidthCharsInValuesFields",
  "fullRequestPath": "http://localhost:8080/Brand/BKFRCGKZDS%E2%80%AEWXNMWPHFTC",
  "contractPath": "/Brand/{id}",
  "server": "http://localhost:8080"
}
```

### Key Fields

| Field                       | Description                                                                                           |
|-----------------------------|-------------------------------------------------------------------------------------------------------|
| `testId`                    | Unique test identifier (e.g. "Test 449"). Note: filenames strip the space (e.g. `Test449.json`)       |
| `traceId`                   | UUID for correlating with server-side logs (also sent as `X-Dochia-Trace-Id` header)                  |
| `scenario`                  | Human-readable description of what the test does                                                      |
| `expectedResult`            | What the test expected                                                                                |
| `result`                    | Outcome: `"error"`, `"warning"`, `"success"`, or `"skipped"`                                          |
| `resultReason`              | Short reason for the result                                                                           |
| `resultDetails`             | Detailed explanation of the result                                                                    |
| `request.payload`           | The exact request body sent                                                                           |
| `request.httpMethod`        | HTTP method used                                                                                      |
| `request.url`               | Full URL including path parameters                                                                    |
| `response.responseCode`     | HTTP status code (9XX codes are Dochia-specific, use `dochia explain --type response_code` to decode) |
| `response.jsonBody`         | Parsed response body                                                                                  |
| `response.responseTimeInMs` | Response latency                                                                                      |
| `playbook`                  | Name of the playbook that generated this test                                                         |
| `path`                      | OpenAPI path template                                                                                 |
| `contractPath`              | Original path from the OpenAPI contract                                                               |

## Summary Report JSON

`dochia-summary-report.json` contains an array of all test results in a compact format:

```json
{
  "testCases": [
    {
      "id": "Test 1",
      "scenario": "Send [values containing abugidas characters] in request fields...",
      "result": "error",
      "resultReason": "Unexpected behaviour 953",
      "resultDetails": "Unexpected behaviour: expected [200, 201, 202, 204], actual [953]",
      "playbook": "AbugidasInStringFields",
      "path": "/Brand/{id}",
      "httpMethod": "put",
      "httpResponseCode": 953,
      "responseBody": "...",
      "timeToExecuteInMs": "1",
      "timeToExecuteInSec": 0.001,
      "switchedResult": false
    }
  ],
  "totalTests": "447",
  "success": "0",
  "warnings": "33",
  "errors": "414",
  "executionTime": "2",
  "timestamp": "Sat, 28 Mar 2026 20:46:46 +0200",
  "dochiaVersion": "1.3.1"
}
```

### Summary Key Fields

| Field               | Description                                                              |
|---------------------|--------------------------------------------------------------------------|
| `id`                | Test identifier (e.g. "Test 1"). Filename strips the space: `Test1.json` |
| `result`            | `"error"`, `"warning"`, `"success"`, or `"skipped"`                      |
| `playbook`          | Playbook name                                                            |
| `path`              | OpenAPI path                                                             |
| `httpMethod`        | HTTP method                                                              |
| `httpResponseCode`  | Response status code                                                     |
| `timeToExecuteInMs` | Execution time                                                           |
| `switchedResult`    | Whether the result was switched by a filter                              |

### Summary Metadata Fields

| Field           | Description                     |
|-----------------|---------------------------------|
| `totalTests`    | Total number of tests executed  |
| `success`       | Number of successful tests      |
| `warnings`      | Number of tests with warnings   |
| `errors`        | Number of tests with errors     |
| `executionTime` | Total execution time in seconds |
| `timestamp`     | When the test run started       |
| `dochiaVersion` | Version of Dochia used          |

## Parsing Reports Programmatically

```bash
# Count errors in summary
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error")] | length'

# List unique failing playbooks
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error") | .playbook] | unique'

# List failing paths
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.result == "error") | .path] | unique'

# Get details of a specific test
cat dochia-report/Test449.json | jq .

# Extract all 5XX errors
cat dochia-report/dochia-summary-report.json | jq '[.testCases[] | select(.httpResponseCode >= 500 and .httpResponseCode < 600)]'

# Get test IDs for replay
cat dochia-report/dochia-summary-report.json | jq -r '[.testCases[] | select(.result == "error") | .id] | join(",")'
```
