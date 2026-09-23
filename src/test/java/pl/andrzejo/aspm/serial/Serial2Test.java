/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

class Serial2Test {
    private final Serial2.Port port = mock(Serial2.Port.class);
    private SerialPortDataListener listener;

    @BeforeEach
    void setUp() {
        when(port.open()).thenReturn(true);
        when(port.isOpen()).thenReturn(true);
        when(port.close()).thenReturn(true);
        when(port.flushIOBuffers()).thenReturn(true);
        when(port.addDataListener(any(SerialPortDataListener.class))).thenReturn(true);
        when(port.write(any(byte[].class), anyInt(), anyInt())).thenAnswer(call -> call.getArguments()[1]);
    }

    @Test
    void shouldContinuePartialWritesFromTheCorrectOffset() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        byte[] bytes = {1, 2, 3, 4, 5};
        when(port.write(any(byte[].class), anyInt(), anyInt())).thenReturn(2, 1, 2);

        //when
        serial.write(bytes);

        //then
        InOrder order = inOrder(port);
        order.verify(port).write(bytes, 5, 0);
        order.verify(port).write(bytes, 3, 2);
        order.verify(port).write(bytes, 2, 3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldReportIncompleteWritesAndStopWhenNoMoreBytesCanBeWritten(int result) throws Exception {
        //given
        RecordingSerial serial = createSerial();
        when(port.write(any(byte[].class), anyInt(), anyInt())).thenReturn(2, result);

        //when
        SerialException failure = assertThrows(SerialException.class, () -> serial.write("hello"));

        //then
        assertThat(failure).hasMessageContaining("after 2 of 5 bytes");
        verify(port, times(2)).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    void shouldUseTheCheckedWritePathForSingleBytesAndRejectAllWritesAfterClose() throws Exception {
        //given
        RecordingSerial serial = createSerial();

        //when
        serial.write(255);
        serial.dispose();

        //then
        verify(port).write(new byte[]{(byte) 255}, 1, 0);
        assertThat(serial.isOpen()).isFalse();
        assertThrows(SerialException.class, () -> serial.write(1));
        assertThrows(SerialException.class, () -> serial.write("text"));
        assertThrows(SerialException.class, () -> serial.write(new byte[]{1}));
        verify(port, times(1)).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    void shouldDecodeSplitUtf8AndLargeChunksInOrderOutsideTheSerialMonitor() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        String text = "ą" + String.join("", Collections.nCopies(10_000, "x😀"));
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        //when
        receive(new byte[]{bytes[0]});
        assertThat(serial.messages).isEmpty();
        receive(java.util.Arrays.copyOfRange(bytes, 1, bytes.length));

        //then
        assertThat(String.join("", serial.messages)).isEqualTo(text);
        assertThat(serial.callbackHeldMonitor).isFalse();
    }

    @Test
    void shouldDiscardIncompleteDecoderInputAlongWithPortBuffers() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        receive(new byte[]{(byte) 0xC4});

        //when
        serial.discardBuffers();
        receive(new byte[]{'A'});

        //then
        assertThat(serial.messages).containsExactly("A");
        verify(port).flushIOBuffers();
    }

    @Test
    void shouldResetDecoderEvenWhenNativeBufferDiscardFails() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        receive(new byte[]{(byte) 0xC4});
        when(port.flushIOBuffers()).thenReturn(false);

        //when
        assertThrows(SerialException.class, serial::discardBuffers);
        receive(new byte[]{'A'});

        //then
        assertThat(serial.messages).containsExactly("A");
    }

    @Test
    void shouldNotDecodeOldBytesWithANewCharset() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        receive(new byte[]{(byte) 0xC4});

        //when
        serial.setCharset(StandardCharsets.UTF_16LE);
        receive(new byte[]{'A', 0});

        //then
        assertThat(serial.messages).containsExactly("A");
    }

    @Test
    void shouldDiscardTheRemainderOfAnEventWhenCallbackResetsTheDecoder() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        serial.afterMessage = () -> {
            try {
                serial.discardBuffers();
            } catch (SerialException e) {
                throw new AssertionError(e);
            }
        };
        byte[] data = String.join("", Collections.nCopies(10_000, "x")).getBytes(StandardCharsets.UTF_8);

        //when
        receive(data);

        //then
        assertThat(serial.messages).hasSize(1);
        assertThat(serial.messages.get(0)).hasSize(8192);
    }

    @Test
    void shouldRetainThePortForRetryWhenNativeCloseFails() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        when(port.close()).thenReturn(false, true);

        //when
        IOException failure = assertThrows(IOException.class, serial::dispose);

        //then
        assertThat(failure).hasMessageContaining("Failed to close serial port");
        assertThat(serial.isOpen()).isFalse();
        assertThrows(SerialException.class, () -> serial.write("ignored"));

        //when
        serial.dispose();
        serial.dispose();
        receive(new byte[]{'A'});

        //then
        verify(port, times(2)).close();
        assertThat(serial.messages).isEmpty();
    }

    @Test
    void shouldAttemptCloseEvenWhenRemovingTheListenerThrows() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        doThrow(new IllegalStateException("listener failure")).when(port).removeDataListener();

        //when
        IOException failure = assertThrows(IOException.class, serial::dispose);
        serial.dispose();

        //then
        verify(port).close();
        assertThat(failure).hasMessageContaining("listener");
        assertThat(serial.isOpen()).isFalse();
    }

    @Test
    void shouldPreserveBothListenerRemovalAndCloseFailures() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        doThrow(new IllegalStateException("listener failure")).when(port).removeDataListener();
        when(port.close()).thenReturn(false);

        //when
        IOException failure = assertThrows(IOException.class, serial::dispose);

        //then
        assertThat(failure.getCause()).hasMessage("listener failure");
        assertThat(failure.getSuppressed()).hasSize(1);
        assertThat(failure.getSuppressed()[0]).hasMessageContaining("Failed to close serial port");
    }

    @Test
    void shouldCloseAnOpenedPortWhenConstructorFailsLater() {
        //given
        doThrow(new IllegalStateException("control lines failure")).when(port).setControlLines(false, false);

        //when
        SerialException failure = assertThrows(SerialException.class, this::createSerial);

        //then
        verify(port).removeDataListener();
        verify(port).close();
        assertThat(failure.getCause()).hasMessage("control lines failure");
    }

    @Test
    void shouldCleanUpFailedListenerRegistrationAndPreserveCleanupFailure() {
        //given
        when(port.addDataListener(any(SerialPortDataListener.class))).thenReturn(false);
        when(port.close()).thenReturn(false);

        //when
        SerialException failure = assertThrows(SerialException.class, this::createSerial);

        //then
        verify(port).close();
        assertThat(failure.getCause()).hasMessageContaining("Failed to register");
        assertThat(failure.getCause().getSuppressed()).hasSize(1);
    }

    @Test
    void shouldAllowTheListenerToFinishWhileDisposeWaitsForIt() throws Exception {
        //given
        RecordingSerial serial = createSerial();
        CountDownLatch callbackEntered = new CountDownLatch(1);
        CountDownLatch removingListener = new CountDownLatch(1);
        serial.afterMessage = () -> {
            callbackEntered.countDown();
            await(removingListener);
            assertThat(serial.isOpen()).isFalse();
            IOException failure = assertThrows(IOException.class, serial::dispose);
            assertThat(failure).hasMessageContaining("already in progress");
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> callback = executor.submit(() -> receive(new byte[]{'A'}));
            await(callbackEntered);
            doAnswer(call -> {
                removingListener.countDown();
                callback.get(2, TimeUnit.SECONDS);
                return null;
            }).when(port).removeDataListener();

            //when
            serial.dispose();

            //then
            callback.get(2, TimeUnit.SECONDS);
            verify(port).close();
            assertThat(serial.messages).containsExactly("A");
            assertThat(serial.callbackHeldMonitor).isFalse();
        } finally {
            removingListener.countDown();
            executor.shutdownNow();
        }
    }

    private RecordingSerial createSerial() throws SerialException {
        RecordingSerial serial = new RecordingSerial(port);
        ArgumentCaptor<SerialPortDataListener> captor = ArgumentCaptor.forClass(SerialPortDataListener.class);
        verify(port).addDataListener(captor.capture());
        listener = captor.getValue();
        return serial;
    }

    private void receive(byte[] bytes) {
        listener.serialEvent(new SerialPortEvent(mock(SerialPort.class),
                SerialPort.LISTENING_EVENT_DATA_RECEIVED, bytes));
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(2, TimeUnit.SECONDS), "Timed out waiting for the other thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private static class RecordingSerial extends Serial2 {
        private final List<String> messages = new ArrayList<>();
        private Runnable afterMessage = () -> {
        };
        private boolean callbackHeldMonitor;

        RecordingSerial(Port port) throws SerialException {
            super("test", 9600, 'N', 8, 1, false, false, name -> port);
        }

        @Override
        protected void message(char[] buff, int length) {
            callbackHeldMonitor |= Thread.holdsLock(this);
            messages.add(new String(buff, 0, length));
            afterMessage.run();
        }
    }
}
