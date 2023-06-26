/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.backend;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class TestScheduledExecutorService implements ScheduledExecutorService {

    private static class DoneFuture<V> implements ScheduledFuture<V> {

        private final V value;

        DoneFuture(V value) {
            this.value = value;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return value;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return value;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }
    }

    private boolean shutdown = false;
    private long lastInitialDelaySet;
    private long lastPeriodSet;

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return true;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            return new DoneFuture<>(task.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        task.run();
        return new DoneFuture<>(result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return new DoneFuture<>(null);
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return tasks.stream().map(t -> {
            try {
                return t.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).map(DoneFuture::new).collect(Collectors.toList());
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout,
            TimeUnit unit) throws InterruptedException {
        return invokeAll(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return tasks.stream().map(t -> {
            try {
                return t.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).findAny().get();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(tasks);
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay,
            TimeUnit unit) {
        return new DoneFuture<>(null);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
            TimeUnit unit) {
        try {
            return new DoneFuture<>(callable.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        lastInitialDelaySet = initialDelay;
        lastPeriodSet = period;
        command.run();
        return new DoneFuture<>(null);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay, long delay, TimeUnit unit) {
        command.run();
        return new DoneFuture<>(null);
    }

    long getLastInitialDelaySet() {
        return lastInitialDelaySet;
    }

    long getLastPeriodSet() {
        return lastPeriodSet;
    }
}
