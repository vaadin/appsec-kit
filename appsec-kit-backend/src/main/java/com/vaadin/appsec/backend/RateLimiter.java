/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rate limiter for limiting the calls to an API.
 */
class RateLimiter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RateLimiter.class);

    private final Semaphore semaphore;
    private final int ratePerSecond;
    private long lastRequestTime;

    RateLimiter(int ratePerSecond) {
        this.semaphore = new Semaphore(ratePerSecond);
        this.ratePerSecond = ratePerSecond;
        this.lastRequestTime = System.currentTimeMillis();
    }

    void limit() throws InterruptedException {
        semaphore.acquire();
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastRequestTime;
        long timeToWait = 1000L / ratePerSecond - timeElapsed;
        if (timeToWait > 0) {
            LOGGER.debug("Apply rate limit");
            Thread.sleep(timeToWait);
        }
        lastRequestTime = System.currentTimeMillis();
        semaphore.release();
    }
}
