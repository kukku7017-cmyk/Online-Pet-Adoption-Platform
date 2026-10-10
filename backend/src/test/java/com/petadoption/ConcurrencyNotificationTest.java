package com.petadoption;

import com.petadoption.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyNotificationTest {

    @Test
    @DisplayName("CONC-01: Bounded ExecutorService executes concurrent async tasks safely")
    public void testConcurrentAsyncExecution() throws InterruptedException {
        NotificationService service = NotificationService.getInstance();
        int taskCount = 15;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            service.submitAsyncTask(() -> {
                try {
                    Thread.sleep(20);
                    successCounter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completedInTime = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completedInTime, "All asynchronous background tasks should complete within timeout");
        assertEquals(taskCount, successCounter.get(), "Task counter must match total submitted tasks");
    }
}
