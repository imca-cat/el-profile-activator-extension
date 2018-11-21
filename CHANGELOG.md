# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][1], and this project adheres
to [Semantic Versioning][2].

## Unreleased

## 0.2.0 - 2018-11-21

### Changed

* Namespaced the magic property name by prefixing it with "paa:", so now
  the magic property name is "paa:mvel", not "mvel".  To upgrade, change
  all occurrences of the "mvel" magic property name to "paa:mvel".

* Upgraded the mvel2 dependency to 2.4.3.Final.

## 0.1.0 - 2018-11-07

* First public release.

[1]: https://keepachangelog.com/en/1.0.0/
[2]: https://semver.org/spec/v2.0.0.html
