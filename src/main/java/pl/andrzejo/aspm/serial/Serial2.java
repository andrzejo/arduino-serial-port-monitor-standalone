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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;


public class Serial2 implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Serial2.class);
    private static final int BUFFER_CAPACITY = 8192;
    private SerialPort port;
    private CharsetDecoder decoder;
    private final ByteBuffer inByteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final CharBuffer outCharBuffer = CharBuffer.allocate(BUFFER_CAPACITY);

    public Serial2(DeviceConfig config) throws SerialException {
        this(config.getDevice(), config.getBaud(), config.getParity(),
                config.getDataBits(), config.getStopBits(), config.isRTS(), config.isDTR());
    }

    public Serial2(String portName, int baudRate, char parity, int dataBits, float stopBits,
                   boolean setRTS, boolean setDTR) throws SerialException {

        setCharset(StandardCharsets.UTF_8);

        if ("none".equalsIgnoreCase(portName)) {
            return;
        }

        try {
            port = SerialPort.getCommPort(portName);
            port.setBaudRate(baudRate);
            port.setNumDataBits(dataBits);
            port.setNumStopBits(mapStopBits(stopBits));
            port.setParity(mapParity(parity));
            port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

            if (!port.openPort()) {
                throw new SerialException("Failed to open serial port: " + portName);
            }

            if (setDTR) {
                port.setDTR();
            } else {
                port.clearDTR();
            }

            if (setRTS) {
                port.setRTS();
            } else {
                port.clearRTS();
            }

            inByteBuffer.clear();
            outCharBuffer.clear();

            port.addDataListener(new SerialPortDataListener() {
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
            log.info("Serial port {} opened successfully ({} baud)", portName, baudRate);
        } catch (Exception e) {
            String errorMsg = String.format("Error opening serial port '%s': %s", portName, e.getMessage());
            log.error(errorMsg, e);
            throw new SerialException(errorMsg, e);
        }
    }

    public synchronized void discardBuffers() {
        if (port != null && port.isOpen()) {
            port.flushIOBuffers();
        }
    }

    private synchronized void decodeAndDispatch(byte[] bytes) {
        int offset = 0;

        while (offset < bytes.length || inByteBuffer.position() > 0) {
            int toCopy = Math.min(bytes.length - offset, inByteBuffer.remaining());
            if (toCopy > 0) {
                inByteBuffer.put(bytes, offset, toCopy);
                offset += toCopy;
            }

            inByteBuffer.flip();
            decoder.decode(inByteBuffer, outCharBuffer, false);
            inByteBuffer.compact();

            if (outCharBuffer.position() == 0) {
                break;
            }

            outCharBuffer.flip();
            char[] chars = new char[outCharBuffer.remaining()];
            outCharBuffer.get(chars);
            outCharBuffer.clear();
            message(chars, chars.length);
        }
    }

    protected void message(char[] buff, int length) {
    }

    public synchronized void write(byte[] bytes) {
        if (port != null && port.isOpen()) {
            port.writeBytes(bytes, bytes.length);
        }
    }

    public void write(String text) {
        if (text != null) {
            write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void write(int singleByte) {
        if (port != null && port.isOpen()) {
            port.writeBytes(new byte[]{(byte) singleByte}, 1);
        }
    }

    public boolean isOpen() {
        return port != null && port.isOpen();
    }

    @Override
    public void close() throws IOException {
        dispose();
    }

    public void dispose() throws IOException {
        SerialPort portToClose;

        synchronized (this) {
            portToClose = port;
            port = null;
        }

        if (portToClose == null) {
            return;
        }

        try {
            if (portToClose.isOpen()) {
                portToClose.removeDataListener();
                portToClose.closePort();
                log.info("Serial port closed.");
            }
        } catch (Exception e) {
            throw new IOException("Failed to close serial port", e);
        }
    }

    public synchronized void setCharset(Charset charset) {
        this.decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    private static int mapParity(char parity) {
        switch (Character.toUpperCase(parity)) {
            case 'E':
                return SerialPort.EVEN_PARITY;
            case 'O':
                return SerialPort.ODD_PARITY;
            case 'M':
                return SerialPort.MARK_PARITY;
            case 'S':
                return SerialPort.SPACE_PARITY;
            default:
                return SerialPort.NO_PARITY;
        }
    }

    private static int mapStopBits(float stopBits) {
        if (Float.compare(stopBits, 1.5f) == 0) return SerialPort.ONE_POINT_FIVE_STOP_BITS;
        if (Float.compare(stopBits, 2.0f) == 0) return SerialPort.TWO_STOP_BITS;
        return SerialPort.ONE_STOP_BIT;
    }

}
