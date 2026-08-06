# metrics-druid

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-8-orange)](https://github.com/easy-4-java/metrics-druid) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> **Status**: maintained on the `feature/1.0.x` line (JDK 8). Artifacts are not yet published to Maven Central; they are distributed through the project's private repository and GitHub Releases.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`metrics-druid` bridges [Dropwizard Metrics](https://metrics.dropwizard.io/) health checks with [Alibaba Druid](https://github.com/alibaba/druid) connection-pool statistics. It currently ships a single `HealthCheck` (`io.github.easy4j.metrics.druid.DatabaseHealthCheck`) that pokes Druid's `DruidStatService` (wall-stat map, data-source instances, MBean registration) and reports `Result.healthy()`.

What it is:

- A Dropwizard `HealthCheck` for Druid-managed data sources — register it in a `HealthCheckRegistry` next to your other checks;
- A starting point for Druid JDBC monitoring integration (`Metrics + Druid for JDBC monitoring`).

What it is not:

- Not a metrics reporter for Druid pool gauges — no per-pool `Gauge`s are registered yet;
- Not a replacement for Druid's own monitoring (Druid Monitor / `DruidStatService`).

Typical scenarios:

| Scenario | What to use |
| :--- | :--- |
| Druid datasource liveness in a health endpoint | `DatabaseHealthCheck` registered in a `HealthCheckRegistry` |
| Verifying Druid stat service is reachable | `DatabaseHealthCheck` (touches wall-stat map + registered instances) |

## 2. Features & Status

| Capability | Status | Notes |
| :--- | :--- | :--- |
| Druid-aware health check | Implemented | `DatabaseHealthCheck` extends `com.codahale.metrics.health.HealthCheck` |
| Druid stat-service touch | Implemented | `DruidStatService.getWallStatMap`, `DruidDataSourceStatManager.getInstances`, `DruidStatService.registerMBean` |
| Pool gauges / reporters | Not yet implemented | Only the health check exists in this branch |
| Tests | Not yet present | No `src/test` in this branch |

## 3. Requirements & Compatibility

| Item | Requirement |
| :--- | :--- |
| JDK | 8+ |
| Maven | 3.0+ (Maven Wrapper `mvnw` included) |
| Dependencies | druid 1.1.20, metrics-core 4.1.1, metrics-healthchecks, slf4j-api 2.0.18, javax.servlet-api (provided), lombok (provided); junit 4.13.2 (test) |

Version lines:

| Branch | JDK | Version pattern |
| :--- | :---: | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. Architecture & Modules

```text
HealthCheckRegistry (application)
        |
        v
DatabaseHealthCheck (io.github.easy4j.metrics.druid)
        |
        +--> DruidStatService (wall stats, MBean registration)
        `--> DruidDataSourceStatManager (registered data sources)
        |
        v
Result.healthy() / Result.unhealthy(...)
```

Single-module jar with one class: `io.github.easy4j.metrics.druid.DatabaseHealthCheck` (extends `com.codahale.metrics.health.HealthCheck`).

## 5. Installation

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>metrics-druid</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:metrics-druid:1.0.x.20260630-SNAPSHOT'
```

The snapshot is served from the project's private repository (see `distributionManagement` in the pom). No Maven Central release is available yet.

## 6. Quick Start

```java
import com.codahale.metrics.health.HealthCheckRegistry;
import io.github.easy4j.metrics.druid.DatabaseHealthCheck;

HealthCheckRegistry healthChecks = new HealthCheckRegistry();
healthChecks.register("druid-database", new DatabaseHealthCheck());

HealthCheck.Result result = healthChecks.runHealthCheck("druid-database");
// result.isHealthy() == true while Druid stat services respond
```

The check returns `Result.healthy()` after successfully touching the Druid wall-stat map, the registered data-source instances and MBean registration.

## 7. Configuration

No configuration properties. The check uses the Druid stat service singletons (`DruidStatService.getInstance()`, `DruidDataSourceStatManager`) directly; Druid itself is configured as usual (its own `statViewServlet` / monitor settings are outside this module).

## 8. Core Usage / API

The module's full API surface is `DatabaseHealthCheck` (default constructor) overriding `HealthCheck.check()`. Use it anywhere a `HealthCheck` is accepted — e.g. `HealthCheckRegistry.register(...)`, or servlet health-check endpoints that run registered checks.

## 9. Testing & Build

```bash
./mvnw clean verify
```

The build is configured with:

- JUnit 4 + Maven Surefire (no test sources exist in this branch yet);
- JaCoCo coverage reporting plus a line-coverage check rule with a 90% minimum target (`haltOnFailure=false`);
- Source and Javadoc jars attached at package time;
- a `central` release profile (GPG signing + Central publishing) reserved for official releases.

## 10. Versioning & Branches

Three parallel version lines, each bound to a JDK baseline:

| Branch | JDK | Version pattern | Maintenance |
| :--- | :---: | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | Current development line |
| `feature/2.0.x` | 17 | `2.0.x.*` | Maintained in parallel |
| `feature/3.0.x` | 21 | `3.0.x.*` | Maintained in parallel |

Snapshots on this branch are versioned `1.0.x.20260630-SNAPSHOT`.

## 11. Contributing & License

Contributions are welcome — open an issue or pull request on GitHub. All source files are licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
