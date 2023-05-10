package com.vaadin.appsec.service;

import java.util.concurrent.Semaphore;

public class RateLimiter {
    private final Semaphore semaphore;
    private final int ratePerSecond;
    private long lastRequestTime;

    public RateLimiter(int ratePerSecond) {
        this.semaphore = new Semaphore(ratePerSecond);
        this.ratePerSecond = ratePerSecond;
        this.lastRequestTime = System.currentTimeMillis();
    }

    public void acquire() throws InterruptedException {
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
