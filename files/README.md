# Examples of files used by dochia

This folder contains examples of various files used by *dochia*. There are also examples on how each file can be used,
as well as links to the *dochia* documentation for additional details.

## Headers File

*dochia* support both argument level headers using `-H` as well as path-level headers using the `--headers <FILE>`
argument.

The [headers.yml](./headers.yml) is an example on how to supply headers to all requests using the `all:` entry,
as well as path-specific headers using the `/pets:` entry.

**Please note that headers must be sub-elements of `all` or specific paths.**

## Reference Data File

Supplying reference data is typically needed for more in-depth testing. You can supply such a file using
the `--ref-data <FILE>` argument.

The [referenceFields.yml](./referenceFields.yml) is an example on how you can supply reference data for all requests
using the `all:` entry, as well as to individual paths using the `/pets/{id}:` entry.

**Please note that reference data entries must be sub-elements of `all` or specific paths.**

## Playbook Configuration

By default, each Playbook has an expected response code based on its specific test case. You can override the expected
response code by providing a properties file through the `--playbooks-config` argument.

The [playbooksConfig.properties](./playbooksConfig.properties) shows how you can override the default response code
for the `DummyAcceptHeaders` Playbook to expect `403` instead of the default `406`.

More documentation here: [--playbooks-config](https://docs.dochia.dev/docs/advanced-topics/testCasePlaybooks-config).

## Custom Mutators

When doing [continuous fuzzing](https://docs.dochia.dev/docs/getting-started/running-dochia/#continuous-fuzzing-mode)
you can supply custom mutators using the `dochia random ... --mutators <FOLDER>`.

The [mutators](./mutators) folder contains some sample mutators that can be used for continuous fuzzing.

[first.yml](./mutators/first.yml) and [second.yml](./mutators/second.yml) provides the mutation values
through an array of values within the yaml file, while the [third.yml](./mutators/third.yml) will load
the values from the [nosql.txt](./nosql.txt) file.

## Error Leaks Keywords

*dochia* automatically checks for error leaks in the responses. It has an internal list of keywords for the most popular
programming languages.
You can supply custom error leak keywords using the `--error-leaks-keywords <FILE>` argument.

The provided keywords are searched as substrings in the response body and if found, the response is marked as an error
leak. Example [errorLeaks.txt](./errorLeaks.txt) file contains some custom keywords that can be used.

## Paths ordering

By default, *dochia* runs paths in alphabetical order. You can override this behavior by providing a file with the
desired
order using the `--paths-run-order <FILE>` argument. An example file [pathsOrder.txt](./pathsOrder.txt).




