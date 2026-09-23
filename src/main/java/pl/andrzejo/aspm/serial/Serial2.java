/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 *
 * Based on Serial.java from Arduino project (https://github.com/arduino/Arduino)
 *
 */

package pl.andrzejo.aspm.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.andrzejo.aspm.settings.types.DeviceConfig;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Objects;
import java.util.function.Function;


public class Serial2 implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Serial2.class);
    private static final int BUFFER_CAPACITY = 8192;
    private Port port;
    private boolean shutdownRequested;
    private boolean closing;
    private long decoderVersion;
    private CharsetDecoder decoder;
    private final ByteBuffer inByteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final CharBuffer outCharBuffer = CharBuffer.allocate(BUFFER_CAPACITY);

    public Serial2(DeviceConfig config) throws SerialException {
        this(config.getDevice(), config.getBaud(), config.getParity(),
                config.getDataBits(), config.getStopBits(), config.isRTS(), config.isDTR());
    }

    public Serial2(String portName, int baudRate, char parity, int dataBits, float stopBits,
                   boolean setRTS, boolean setDTR) throws SerialException {
        this(portName, baudRate, parity, dataBits, stopBits, setRTS, setDTR, JSerialCommPort::new);
    }

    Serial2(String portName, int baudRate, char parity, int dataBits, float stopBits,
            boolean setRTS, boolean setDTR, Function<String, Port> portFactory) throws SerialException {
        setCharset(StandardCharsets.UTF_8);

        if ("none".equalsIgnoreCase(portName)) {
            return;
        }

        try {
            port = portFactory.apply(portName);
            port.configure(baudRate, parity, dataBits, stopBits);

            if (!port.open()) {
                throw new SerialException("Failed to open serial port: " + portName);
            }

            port.setControlLines(setRTS, setDTR);

            registerDataListener(portName);
            log.info("Serial port {} opened successfully ({} baud)", portName, baudRate);
        } catch (Exception e) {
            cleanupAfterOpenFailure(e);
            String errorMsg = String.format("Error opening serial port '%s': %s", portName, e.getMessage());
            log.error(errorMsg, e);
            throw new SerialException(errorMsg, e);
        }
    }

    private void registerDataListener(String portName) throws SerialException {
        boolean registered = port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] received = event.getReceivedData();
                if (received != null && received.length > 0) {
                    decodeAndDispatch(received);
                }
            }
        });
        if (!registered) {
            throw new SerialException("Failed to register serial port listener: " + portName);
        }
    }

    private void cleanupAfterOpenFailure(Exception failure) {
        try {
            dispose();
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    public synchronized void discardBuffers() throws SerialException {
        try {
            if (isOpen() && !port.flushIOBuffers()) {
                throw new SerialException("Failed to discard serial port buffers");
            }
        } finally {
            resetDecoder();
        }
    }

    private void decodeAndDispatch(byte[] bytes) {
        long version;
        synchronized (this) {
            version = decoderVersion;
        }
        int offset = 0;
        while (true) {
            char[] chars;
            boolean more;
            synchronized (this) {
                if (shutdownRequested || version != decoderVersion) {
                    return;
                }
                int toCopy = Math.min(bytes.length - offset, inByteBuffer.remaining());
                inByteBuffer.put(bytes, offset, toCopy);
                offset += toCopy;
                inByteBuffer.flip();
                int beforeDecode = inByteBuffer.remaining();
                CoderResult result = decoder.decode(inByteBuffer, outCharBuffer, false);
                boolean consumed = inByteBuffer.remaining() < beforeDecode;
                inByteBuffer.compact();
                outCharBuffer.flip();
                chars = new char[outCharBuffer.remaining()];
                outCharBuffer.get(chars);
                outCharBuffer.clear();
                more = offset < bytes.length || result.isOverflow();
                if (more && toCopy == 0 && !consumed && chars.length == 0) {
                    throw new IllegalStateException("Serial decoder cannot make progress");
                }
            }
            if (chars.length > 0) {
                message(chars, chars.length);
            }
            if (!more) {
                return;
            }
        }
    }

    protected void message(char[] buff, int length) {
    }

    public synchronized void write(byte[] bytes) throws SerialException {
        Objects.requireNonNull(bytes, "bytes");
        if (!isOpen()) {
            throw new SerialException("Serial port is closed");
        }
        int offset = 0;
        try {
            while (offset < bytes.length) {
                int written = port.write(bytes, bytes.length - offset, offset);
                if (written <= 0) {
                    throw new SerialException("Serial write failed after " + offset + " of " + bytes.length
                            + " bytes (result: " + written + ")");
                }
                offset += written;
            }
        } catch (RuntimeException e) {
            throw new SerialException("Serial write failed after " + offset + " of " + bytes.length + " bytes", e);
        }
    }

    public void write(String text) throws SerialException {
        if (text != null) {
            write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void write(int singleByte) throws SerialException {
        write(new byte[]{(byte) singleByte});
    }

    public synchronized boolean isOpen() {
        return !shutdownRequested && port != null && port.isOpen();
    }

    @Override
    public void close() throws IOException {
        dispose();
    }

    public void dispose() throws IOException {
        Port portToClose = beginClose();
        if (portToClose == null) {
            return;
        }

        try {
            IOException removeFailure = safeRemoveDataListener(portToClose);
            IOException closeFailure = safeClosePort(portToClose);
            IOException disposeException = packException(removeFailure, closeFailure);
            if (disposeException != null) {
                throw disposeException;
            }
        } finally {
            synchronized (this) {
                closing = false;
                resetDecoder();
            }
        }
    }

    private IOException packException(IOException removeFailure, IOException closeFailure) {
        if (removeFailure == null) {
            return closeFailure;
        }
        if (closeFailure != null) {
            removeFailure.addSuppressed(closeFailure);
        }
        return removeFailure;
    }

    private IOException safeClosePort(Port portToClose) {
        try {
            closePort(portToClose);
        } catch (IOException closeFailure) {
            return closeFailure;
        }
        return null;
    }

    private static IOException safeRemoveDataListener(Port portToClose) {
        try {
            portToClose.removeDataListener();
        } catch (Exception e) {
            return new IOException("Failed to remove serial port listener", e);
        }
        return null;
    }

    private synchronized Port beginClose() throws IOException {
        if (port == null) {
            return null;
        }
        if (closing) {
            throw new IOException("Serial port close is already in progress");
        }
        shutdownRequested = true;
        closing = true;
        return port;
    }

    private void closePort(Port portToClose) throws IOException {
        boolean closed;
        try {
            closed = portToClose.close();
        } catch (RuntimeException e) {
            throw new IOException("Failed to close serial port", e);
        }
        if (!closed) {
            throw new IOException("Failed to close serial port: port refused to close");
        }
        synchronized (this) {
            port = null;
        }
        log.info("Serial port closed.");
    }

    public synchronized void setCharset(Charset charset) {
        this.decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        resetDecoder();
    }

    private void resetDecoder() {
        decoder.reset();
        inByteBuffer.clear();
        outCharBuffer.clear();
        decoderVersion++;
    }

    interface Port {
        void configure(int baudRate, char parity, int dataBits, float stopBits);

        boolean open();

        void setControlLines(boolean rts, boolean dtr);

        boolean addDataListener(SerialPortDataListener listener);

        void removeDataListener();

        boolean flushIOBuffers();

        int write(byte[] bytes, int length, int offset);

        boolean isOpen();

        boolean close();
    }

}
