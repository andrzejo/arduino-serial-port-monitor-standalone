/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class DeadlockDetectorService {
    private static final Logger logger = LoggerFactory.getLogger(DeadlockDetectorService.class);
    private static final long CHECK_INTERVAL_SECONDS = 5;
    private static final long EDT_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(15);
    private static final long EDT_RECOVERY_GRACE_NANOS = TimeUnit.SECONDS.toNanos(15);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ThreadMXBean threadMXBean;
    private final LongSupplier nanoTime;
    private final Consumer<Runnable> dispatchToEdt;
    private final Runnable terminate;
    private final Supplier<ScheduledExecutorService> executorFactory;

    private ScheduledExecutorService executor;
    private EdtProbe pendingProbe;
    private boolean edtWarningReported;
    private long edtWarningTime;
    private boolean terminating;

    public DeadlockDetectorService() {
        this(ManagementFactory.getThreadMXBean(), System::nanoTime, SwingUtilities::invokeLater,
                DeadlockDetectorService::terminateJvm, () -> newDaemonExecutor("deadlock-detector"));
    }

    DeadlockDetectorService(ThreadMXBean threadMXBean, LongSupplier nanoTime,
                            Consumer<Runnable> dispatchToEdt, Runnable terminate,
                            Supplier<ScheduledExecutorService> executorFactory) {
        this.threadMXBean = threadMXBean;
        this.nanoTime = nanoTime;
        this.dispatchToEdt = dispatchToEdt;
        this.terminate = terminate;
        this.executorFactory = executorFactory;
    }

    private static ScheduledExecutorService newDaemonExecutor(String name) {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        });
    }

    public synchronized void start() {
        if (executor != null || terminating) {
            return;
        }
        ScheduledExecutorService startedExecutor = executorFactory.get();
        executor = startedExecutor;
        try {
            startedExecutor
                    .scheduleWithFixedDelay(() -> check(startedExecutor),
                            CHECK_INTERVAL_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            executor = null;
            startedExecutor.shutdownNow();
            throw e;
        }
    }

    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        pendingProbe = null;
        edtWarningReported = false;
    }

    private void check(ScheduledExecutorService startedExecutor) {
        try {
            synchronized (this) {
                if (executor != startedExecutor || terminating) {
                    return;
                }
                if (!checkDeadlocks() && !checkEdt()) {
                    return;
                }
                terminating = true;
                startedExecutor.shutdown();
            }
            terminate.run();
        } catch (Throwable e) {
            logger.error("Deadlock detector failed", e);
        }
    }

    private boolean checkDeadlocks() {
        long[] threadIds;
        try {
            threadIds = threadMXBean.isSynchronizerUsageSupported()
                    ? threadMXBean.findDeadlockedThreads()
                    : threadMXBean.findMonitorDeadlockedThreads();
        } catch (RuntimeException e) {
            logger.error("Deadlock scan failed; continuing with the EDT check", e);
            return false;
        }

        if (threadIds == null) {
            return false;
        }

        logger.error("DEADLOCK DETECTED");
        try {
            ThreadInfo[] infos = threadMXBean.getThreadInfo(threadIds, threadMXBean.isObjectMonitorUsageSupported(),
                    threadMXBean.isSynchronizerUsageSupported());
            Arrays
                    .stream(infos)
                    .forEach(info -> logger.error("Deadlocked thread:\n{}", info));
            dumpAllThreads();
        } catch (RuntimeException e) {
            logger.error("Could not collect deadlock diagnostics", e);
        }
        return true;
    }

    private boolean checkEdt() {
        long now = nanoTime.getAsLong();
        if (pendingProbe != null && pendingProbe.responded) {
            if (edtWarningReported) {
                logger.info("EDT is responding again; shutdown cancelled");
            }
            pendingProbe = null;
            edtWarningReported = false;
        }

        if (pendingProbe == null) {
            EdtProbe probe = new EdtProbe(now);
            dispatchToEdt.accept(() -> probe.responded = true);
            pendingProbe = probe;
            return false;
        }

        long waitingNanos = now - pendingProbe.sentAt;
        if (!edtWarningReported && waitingNanos >= EDT_TIMEOUT_NANOS) {
            edtWarningReported = true;
            edtWarningTime = now;
            logger.warn("EDT NOT RESPONDING: probe pending for {} ms; allowing another {} ms for recovery",
                    TimeUnit.NANOSECONDS.toMillis(waitingNanos),
                    TimeUnit.NANOSECONDS.toMillis(EDT_RECOVERY_GRACE_NANOS));
            dumpAllThreads();
        } else if (edtWarningReported && now - edtWarningTime >= EDT_RECOVERY_GRACE_NANOS) {
            if (!pendingProbe.responded) {
                logger.error("EDT still not responding after the recovery grace period; shutting down");
                return true;
            }
        }
        return false;
    }

    private void dumpAllThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        StringBuilder dump = new StringBuilder("Full thread dump:\n");
        for (Map.Entry<Thread, StackTraceElement[]> entry : threads.entrySet()) {
            Thread thread = entry.getKey();
            dump
                    .append('"')
                    .append(thread.getName())
                    .append("\" id=")
                    .append(thread.getId()).append(" state=")
                    .append(thread.getState()).append('\n');
            for (StackTraceElement element : entry.getValue()) {
                dump.append("\tat ").append(element).append('\n');
            }
            dump.append('\n');
        }
        logger.error("{}", dump);
    }

    static void terminateJvm() {
        ScheduledExecutorService watchdog = newDaemonExecutor("deadlock-shutdown-watchdog");
        watchdog.schedule(() -> Runtime.getRuntime().halt(1), SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        System.exit(1);
    }

    private static final class EdtProbe {
        private final long sentAt;
        private volatile boolean responded;

        private EdtProbe(long sentAt) {
            this.sentAt = sentAt;
        }
    }
}
