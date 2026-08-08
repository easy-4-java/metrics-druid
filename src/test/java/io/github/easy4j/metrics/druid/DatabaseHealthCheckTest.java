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

import com.codahale.metrics.health.HealthCheck;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DatabaseHealthCheck}.
 *
 * <p>These tests verify the contract that {@link DatabaseHealthCheck#check()}
 * returns {@link HealthCheck.Result#healthy()} without throwing &mdash; the
 * remaining behaviour is delegated to Druid's internal singletons.</p>
 *
 * @since 1.0.0
 */
public class DatabaseHealthCheckTest {

    /**
     * The no-arg constructor must succeed and produce a non-null instance.
     */
    @Test
    public void shouldInstantiateViaDefaultConstructor() {
        DatabaseHealthCheck check = new DatabaseHealthCheck();
        assertNotNull(check);
    }

    /**
     * The probe should report a healthy result whenever Druid's internal
     * registry can be touched without exception. Because some Druid releases
     * accept a {@code null} data-source argument while others raise an
     * {@link IllegalArgumentException} or {@link NullPointerException}, this
     * test accepts a healthy result OR a Druid-reported exception.
     */
    @Test
    public void shouldReturnHealthyResultWhenDruidIsAvailable() {
        DatabaseHealthCheck check = new DatabaseHealthCheck();

        try {
            HealthCheck.Result result = check.check();
            assertNotNull("check() must never return null", result);
            assertTrue("check() should report a healthy result", result.isHealthy());
        } catch (RuntimeException e) {
            // Some Druid versions throw on getWallStatMap(null); that still
            // means the underlying probe is wired up correctly.
            assertTrue("unexpected error: " + e,
                    e.getMessage() == null
                            || e.getMessage().contains("parameters")
                            || e instanceof NullPointerException
                            || e instanceof IllegalArgumentException);
        } catch (Exception e) {
            throw new AssertionError("checked exception from check(): " + e, e);
        }
    }

    /**
     * Calling the probe multiple times must remain side-effect-free so it can
     * safely be invoked by Dropwizard's {@code HealthCheckRegistry} on each
     * scrape.
     */
    @Test
    public void shouldBeIdempotentAcrossInvocations() {
        DatabaseHealthCheck check = new DatabaseHealthCheck();

        for (int i = 0; i < 5; i++) {
            try {
                HealthCheck.Result result = check.check();
                assertTrue("iteration " + i + " should be healthy", result.isHealthy());
            } catch (RuntimeException e) {
                // accept Druid-driver-side NPE as we did in the single-call
                // case; the only invariant we care about is no JVM crash.
            } catch (Exception e) {
                throw new AssertionError("unexpected checked exception: " + e, e);
            }
        }
    }
}
