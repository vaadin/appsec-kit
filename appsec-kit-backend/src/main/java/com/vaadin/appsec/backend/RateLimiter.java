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

/**
 * Rate limiter for limiting the calls to an API.
 */
class RateLimiter {

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
            Thread.sleep(timeToWait);
        }
        lastRequestTime = System.currentTimeMillis();
        semaphore.release();
    }
}
