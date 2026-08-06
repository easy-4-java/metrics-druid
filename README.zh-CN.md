# metrics-druid

[English](./README.md) | [简体中文](./README.zh-CN.md)

> **项目状态**：`feature/1.0.x` 版本线维护中（JDK 8）。制品尚未发布到 Maven Central，通过项目私服与 GitHub Releases 分发。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`metrics-druid` 将 [Dropwizard Metrics](https://metrics.dropwizard.io/) 健康检查与 [阿里巴巴 Druid](https://github.com/alibaba/druid) 连接池统计打通。目前提供 1 个 `HealthCheck`（`io.github.easy4j.metrics.druid.DatabaseHealthCheck`），它会触达 Druid 的 `DruidStatService`（wall 统计 map、数据源实例、MBean 注册）并返回 `Result.healthy()`。

是什么：

- 面向 Druid 数据源的 Dropwizard `HealthCheck`——注册到 `HealthCheckRegistry`，与其他检查并列运行；
- Druid JDBC 监控集成的起点（项目定位：`Metrics + Druid for JDBC monitoring`）。

不是什么：

- 不是 Druid 连接池指标的 reporter——本分支尚未注册任何按池的 `Gauge`；
- 不是 Druid 自带监控（Druid Monitor / `DruidStatService`）的替代品。

典型场景：

| 场景 | 使用 |
| :--- | :--- |
| 健康端点中的 Druid 数据源存活检查 | 将 `DatabaseHealthCheck` 注册进 `HealthCheckRegistry` |
| 验证 Druid 统计服务可用 | `DatabaseHealthCheck`（触达 wall 统计 map 与已注册实例） |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| Druid 感知健康检查 | 已实现 | `DatabaseHealthCheck` 继承 `com.codahale.metrics.health.HealthCheck` |
| Druid 统计服务触达 | 已实现 | `DruidStatService.getWallStatMap`、`DruidDataSourceStatManager.getInstances`、`DruidStatService.registerMBean` |
| 连接池指标 / reporter | 未实现 | 本分支仅有健康检查 |
| 测试 | 暂无 | 本分支无 `src/test` |

## 3. 环境要求与兼容性

| 项目 | 要求 |
| :--- | :--- |
| JDK | 8+ |
| Maven | 3.0+（内置 Maven Wrapper `mvnw`） |
| 依赖 | druid 1.1.20、metrics-core 4.1.1、metrics-healthchecks、slf4j-api 2.0.18、javax.servlet-api（provided）、lombok（provided）；junit 4.13.2（测试） |

版本线：

| 分支 | JDK | 版本模式 |
| :--- | :---: | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

```text
HealthCheckRegistry (应用)
        |
        v
DatabaseHealthCheck (io.github.easy4j.metrics.druid)
        |
        +--> DruidStatService (wall 统计, MBean 注册)
        `--> DruidDataSourceStatManager (已注册数据源)
        |
        v
Result.healthy() / Result.unhealthy(...)
```

单模块 jar，仅 1 个类：`io.github.easy4j.metrics.druid.DatabaseHealthCheck`（继承 `com.codahale.metrics.health.HealthCheck`）。

## 5. 安装

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>metrics-druid</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:metrics-druid:1.0.x.20260630-SNAPSHOT'
```

快照版本由项目私服提供（见 pom 中 `distributionManagement`）。尚未发布 Maven Central 正式版。

## 6. 快速开始

```java
import com.codahale.metrics.health.HealthCheckRegistry;
import io.github.easy4j.metrics.druid.DatabaseHealthCheck;

HealthCheckRegistry healthChecks = new HealthCheckRegistry();
healthChecks.register("druid-database", new DatabaseHealthCheck());

HealthCheck.Result result = healthChecks.runHealthCheck("druid-database");
// Druid 统计服务正常响应时 result.isHealthy() == true
```

该检查成功触达 Druid wall 统计 map、已注册数据源实例与 MBean 注册后返回 `Result.healthy()`。

## 7. 配置

无配置项。检查直接使用 Druid 统计服务单例（`DruidStatService.getInstance()`、`DruidDataSourceStatManager`）；Druid 本身按常规方式配置（其 `statViewServlet` / monitor 设置不在本模块范围内）。

## 8. 核心用法 / API

模块的完整 API 面就是 `DatabaseHealthCheck`（默认构造器）重写 `HealthCheck.check()`。可在任何接受 `HealthCheck` 的位置使用——例如 `HealthCheckRegistry.register(...)`，或运行已注册检查的 servlet 健康检查端点。

## 9. 测试与构建

```bash
./mvnw clean verify
```

构建配置：

- JUnit 4 + Maven Surefire（本分支暂无测试源码）；
- JaCoCo 覆盖率报告 + 行覆盖率检查规则，最低目标 90%（`haltOnFailure=false`）；
- package 阶段附加源码包与 Javadoc 包；
- 提供 `central` 发布 profile（GPG 签名 + Central 发布插件），仅用于正式发布。

## 10. 版本与分支

三条并行版本线，各自绑定一个 JDK 基线：

| 分支 | JDK | 版本模式 | 维护状态 |
| :--- | :---: | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前开发线 |
| `feature/2.0.x` | 17 | `2.0.x.*` | 并行维护 |
| `feature/3.0.x` | 21 | `3.0.x.*` | 并行维护 |

本分支快照版本为 `1.0.x.20260630-SNAPSHOT`。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。所有源码基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可。
