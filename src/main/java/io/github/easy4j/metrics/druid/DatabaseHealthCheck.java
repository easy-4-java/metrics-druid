/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.easy4j.metrics.druid;

import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.alibaba.druid.stat.DruidStatService;
import com.codahale.metrics.health.HealthCheck;

/**
 * Dropwizard {@link HealthCheck} that pings the Druid connection-pool statistics
 * registry to confirm the surrounding JVM can collect JDBC pool metrics.
 *
 * <p>The check executes the cheap operations that any production Druid integration
 * relies on &mdash; reading the {@code WallStatMap}, loading the data-source
 * instances through {@link DruidDataSourceStatManager}, and re-registering the
 * Druid MBean. If any of these steps would normally throw (e.g. due to a JMX
 * mis-configuration) the check will propagate that error and the application
 * deployment will be marked unhealthy.</p>
 *
 * <p>This health check is intentionally tolerant: it never returns
 * {@link HealthCheck.Result#unhealthy(String)} because Druid does not expose a
 * remote "is connected" signal &mdash; it simply validates that the local
 * runtime can interact with Druid's in-process registry.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 * @see HealthCheck
 * @see DruidStatService#getInstance()
 */
public class DatabaseHealthCheck extends HealthCheck {

    /**
     * The Druid statistics service used to inspect runtime JDBC pools.
     * Acquired via the singleton accessor; never {@code null}.
     */
    private DruidStatService statService = DruidStatService.getInstance();

    /**
     * Default, no-argument constructor required by Dropwizard's
     * {@code HealthCheckRegistry} which instantiates checks reflectively.
     */
    public DatabaseHealthCheck() {
    }

    /**
     * Executes the health probe.
     *
     * <p>The probe performs three idempotent side-effect-free operations against
     * Druid's internal JMX layer; a failure in any of them surfaces as a
     * thrown exception, which Dropwizard treats as a failed health check.</p>
     *
     * @return always {@link HealthCheck.Result#healthy()} because Druid's API
     *         does not expose a definitive "connected" status.
     * @throws Exception if a JMX or class-loading failure occurs while touching
     *         Druid's statistics service.
     */
    @Override
    public HealthCheck.Result check() throws Exception {

        // Refresh WallStat counters (no-op in the absence of a configured filter).
        statService.getWallStatMap(null);

        // Force materialisation of any registered Druid data sources.
        DruidDataSourceStatManager.getInstances();

        // Ensure the Druid MBean is registered against the platform MBean server.
        DruidStatService.registerMBean();

        /*
         * A real "is the database reachable" probe would belong here, but Druid
         * does not currently expose such a signal — the surrounding application
         * is expected to wire its own connectivity check.
         */
        return HealthCheck.Result.healthy();
    }
}
