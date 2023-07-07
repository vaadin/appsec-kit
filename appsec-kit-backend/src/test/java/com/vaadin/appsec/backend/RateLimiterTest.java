/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RateLimiterTest {

    @Test
    public void limit_periodIsWithinASecond() throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter(5);

        long start = System.currentTimeMillis();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        long period = System.currentTimeMillis() - start;

        // Verifying 1100 milliseconds for period instead of 1000 because
        // period calculation also takes a couple of milliseconds
        assertTrue(period < 1100);
    }

    @Test
    public void limit_periodIsGraterThanASecond() throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter(5);

        long start = System.currentTimeMillis();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        rateLimiter.limit();
        long period = System.currentTimeMillis() - start;

        assertTrue(period > 1000);
    }
}
