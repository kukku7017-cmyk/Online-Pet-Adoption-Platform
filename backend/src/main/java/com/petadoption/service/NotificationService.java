package com.petadoption.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency Management:
 * Implements a bounded ThreadPoolExecutor for asynchronous non-blocking workloads
 * such as application notifications, background event logging, and email simulations.
 */
public final class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static volatile NotificationService instance;

    private final ExecutorService executor;

    private NotificationService() {
        // Bounded thread pool with 2 core threads, 4 max threads, and bounded queue capacity of 100
        this.executor = new ThreadPoolExecutor(
            2,
            4,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "pet-async-worker-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Graceful backpressure: runs in caller thread if queue is saturated
        );
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void submitAsyncTask(Runnable task) {
        if (executor.isShutdown()) {
            logger.warn("Executor is shut down, executing task synchronously");
            task.run();
            return;
        }
        executor.submit(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                logger.error("Error executing background task: {}", t.getMessage(), t);
            }
        });
    }

    public void shutdown() {
        logger.info("Initiating graceful shutdown of async executor service...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Async executor service shut down cleanly.");
    }
}
