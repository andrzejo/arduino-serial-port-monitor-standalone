package pl.andrzejo.aspm.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

class DeadlockDetectorServiceTest {
    private final ThreadMXBean threads = mock(ThreadMXBean.class);
    private final AtomicLong clock = new AtomicLong();
    private final Queue<Runnable> edtQueue = new ArrayDeque<>();
    private final AtomicInteger terminations = new AtomicInteger();
    private final List<ScheduledExecutorService> executors = new ArrayList<>();
    private DeadlockDetectorService service;
    private Runnable check;

    @BeforeEach
    void setUp() {
        when(threads.isSynchronizerUsageSupported()).thenReturn(true);
        when(threads.isObjectMonitorUsageSupported()).thenReturn(true);
        service = new DeadlockDetectorService(threads, clock::get, edtQueue::add,
                terminations::incrementAndGet, this::getScheduledExecutorService);
        service.start();
        check = scheduledCheck(executors.get(0));
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        executors.add(executor);
        return executor;
    }

    @Test
    void shouldKeepOnlyOneUnansweredProbe() {
        //given
        check.run();

        //when
        advanceSeconds(5);
        check.run();
        advanceSeconds(5);
        check.run();

        //then
        assertEquals(1, edtQueue.size());
        assertEquals(0, terminations.get());
    }

    @Test
    void shouldAllowFullGracePeriodAfterTimeoutAndTerminateOnlyOnce() {
        //given
        check.run();

        //when
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(14);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(1);
        check.run();
        check.run();

        //then
        assertEquals(1, terminations.get());
        verify(executors.get(0)).shutdown();
    }

    @Test
    void shouldStartGracePeriodWhenWarningIsActuallyReported() {
        //given
        check.run();

        //when
        advanceSeconds(120);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(14);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(1);
        check.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldCancelShutdownWhenEdtRecoversAndResetNextProbeDeadline() {
        //given
        check.run();
        advanceSeconds(15);
        check.run();

        //when
        edtQueue.remove().run();
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(0, terminations.get());
        assertEquals(1, edtQueue.size());

        //when
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldNotBlameResponsiveEdtForDelayedDetectorOrDelayedStart() {
        //given

        //when
        advanceSeconds(3600);
        check.run();
        edtQueue.remove().run();
        advanceSeconds(3600);
        check.run();

        //then
        assertEquals(0, terminations.get());
        assertEquals(1, edtQueue.size());
    }

    @Test
    void shouldHandleNanoTimeWraparound() {
        //given
        clock.set(Long.MAX_VALUE - TimeUnit.SECONDS.toNanos(10));
        check.run();

        //when
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(0, terminations.get());

        //when
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldStartOnlyOneExecutorAndIgnoreChecksAfterStop() {
        //given

        //when
        service.start();

        //then
        assertEquals(1, executors.size());

        //when
        service.stop();
        service.stop();
        check.run();

        //then
        verify(executors.get(0)).shutdownNow();
        verify(threads, never()).findDeadlockedThreads();
        assertTrue(edtQueue.isEmpty());
        assertEquals(0, terminations.get());
    }

    @Test
    void shouldIgnorePreviousRunChecksAndEdtRepliesAfterRestart() {
        //given
        check.run();
        Runnable oldReply = edtQueue.remove();

        //when
        service.stop();
        service.start();
        Runnable restartedCheck = scheduledCheck(executors.get(1));
        restartedCheck.run();
        oldReply.run();

        advanceSeconds(15);
        check.run();
        restartedCheck.run();

        //then
        assertEquals(0, terminations.get());
        assertEquals(1, edtQueue.size());

        //when
        advanceSeconds(15);
        restartedCheck.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldTerminateImmediatelyOnConfirmedDeadlock() {
        //given
        long[] ids = {42};
        when(threads.findDeadlockedThreads()).thenReturn(ids);
        when(threads.getThreadInfo(ids, true, true)).thenReturn(new ThreadInfo[0]);

        //when
        check.run();

        //then
        assertEquals(1, terminations.get());
        assertTrue(edtQueue.isEmpty());
    }

    @Test
    void shouldStillTerminateWhenDeadlockDiagnosticsFail() {
        //given
        long[] ids = {42};
        when(threads.findDeadlockedThreads()).thenReturn(ids);
        when(threads.getThreadInfo(ids, true, true)).thenThrow(new SecurityException("No thread details"));

        //when
        check.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldCheckEdtEvenWhenDeadlockScanFails() {
        //given
        when(threads.findDeadlockedThreads()).thenThrow(new SecurityException("No thread access"));

        //when
        check.run();
        advanceSeconds(15);
        check.run();
        advanceSeconds(15);
        check.run();

        //then
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldFallBackToMonitorDeadlocksWhenSynchronizersAreUnsupported() {
        //given
        when(threads.isSynchronizerUsageSupported()).thenReturn(false);
        long[] ids = {42};
        when(threads.findMonitorDeadlockedThreads()).thenReturn(ids);
        when(threads.getThreadInfo(ids, true, false)).thenReturn(new ThreadInfo[0]);

        //when
        check.run();

        //then
        verify(threads, never()).findDeadlockedThreads();
        assertEquals(1, terminations.get());
    }

    @Test
    void shouldRunShutdownHooksDuringNormalTermination() throws Exception {
        //given

        //when
        String output = runShutdownProcess("normal");

        //then
        assertTrue(output.contains("hook-completed"), output);
    }

    @Test
    void shouldForceExitWhenShutdownHookHangs() throws Exception {
        //given

        //when
        String output = runShutdownProcess("blocked");

        //then
        assertTrue(output.contains("hook-started"), output);
        assertFalse(output.contains("hook-completed"), output);
    }

    private String runShutdownProcess(String mode) throws Exception {
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                ShutdownProcess.class.getName(), mode).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Shutdown watchdog did not terminate the JVM");
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }
            assertEquals(1, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }

    public static class ShutdownProcess {
        public static void main(String[] args) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("hook-started");
                if ("blocked".equals(args[0])) {
                    try {
                        new CountDownLatch(1).await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("hook-completed");
            }));
            DeadlockDetectorService.terminateJvm();
        }
    }

    private Runnable scheduledCheck(ScheduledExecutorService executor) {
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleWithFixedDelay(task.capture(), eq(5L), eq(5L), eq(TimeUnit.SECONDS));
        return task.getValue();
    }

    private void advanceSeconds(long seconds) {
        clock.addAndGet(TimeUnit.SECONDS.toNanos(seconds));
    }
}
