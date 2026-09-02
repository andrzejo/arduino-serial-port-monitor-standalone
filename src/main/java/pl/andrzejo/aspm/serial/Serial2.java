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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
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

public class Serial2 implements SerialPortEventListener, Closeable {
    private static final Logger log = LoggerFactory.getLogger(Serial2.class);

    private static final int BUFFER_CAPACITY = 8192;

    private SerialPort port;
    private CharsetDecoder decoder;
    private final ByteBuffer inByteBuffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    private final CharBuffer outCharBuffer = CharBuffer.allocate(BUFFER_CAPACITY);

    public Serial2(String portName, int baudRate) throws SerialException {
        this(portName, baudRate, 'N', 8, 1.0f, false, false);
    }

    public Serial2(DeviceConfig config) throws SerialException {
        this(config.getDevice(), config.getBaud(), config.getParity(),
                config.getDataBits(), config.getStopBits(), config.isRTS(), config.isDTR());
    }

    public Serial2(String portName, int baudRate, char parity, int dataBits, float stopBits,
                   boolean setRTS, boolean setDTR) throws SerialException {

        // Domyślne kodowanie UTF-8
        setCharset(StandardCharsets.UTF_8);

        if ("none".equalsIgnoreCase(portName)) {
            return;
        }

        try {
            port = new SerialPort(portName);
            port.openPort();
            port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

            int jsscParity = mapParity(parity);
            int jsscStopBits = mapStopBits(stopBits);

            boolean paramsOk = port.setParams(baudRate, dataBits, jsscStopBits, jsscParity, setRTS, setDTR);
            if (!paramsOk) {
                log.warn("Could not set port parameters: {} baud, parity={}, dataBits={}, stopBits={}",
                        baudRate, parity, dataBits, stopBits);
            }

            port.addEventListener(this);
            log.info("Serial port {} opened successfully ({} baud)", portName, baudRate);
        } catch (SerialPortException e) {
            String errorMsg = String.format("Error opening serial port '%s': %s", portName, e.getExceptionType());
            log.error(errorMsg, e);
            throw new SerialException(errorMsg, e);
        }
    }


    @Override
    public synchronized void serialEvent(SerialPortEvent event) {
        if (!event.isRXCHAR() || event.getEventValue() <= 0 || port == null) {
            return;
        }

        try {
            byte[] rawBytes = port.readBytes(event.getEventValue());
            if (rawBytes != null && rawBytes.length > 0) {
                decodeAndDispatch(rawBytes);
            }
        } catch (SerialPortException e) {
            log.error("Error reading from serial port", e);
        }
    }

    private void decodeAndDispatch(byte[] bytes) {
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
        if (port != null && port.isOpened()) {
            try {
                port.writeBytes(bytes);
            } catch (SerialPortException e) {
                log.error("Error writing bytes to serial port", e);
            }
        }
    }

    public void write(String text) {
        if (text != null) {
            write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void write(int singleByte) {
        if (port != null && port.isOpened()) {
            try {
                port.writeInt(singleByte & 0xFF);
            } catch (SerialPortException e) {
                log.error("Error writing byte to serial port", e);
            }
        }
    }

    public void setDTR(boolean state) {
        try {
            if (port != null && port.isOpened()) port.setDTR(state);
        } catch (SerialPortException e) {
            log.error("Failed to set DTR to {}", state, e);
        }
    }

    public void setRTS(boolean state) {
        try {
            if (port != null && port.isOpened()) port.setRTS(state);
        } catch (SerialPortException e) {
            log.error("Failed to set RTS to {}", state, e);
        }
    }

    public boolean isOpen() {
        return port != null && port.isOpened();
    }

    @Override
    public synchronized void close() throws IOException {
        dispose();
    }

    public synchronized void dispose() throws IOException {
        if (port != null) {
            try {
                if (port.isOpened()) {
                    port.closePort();
                    log.info("Serial port closed.");
                }
            } catch (SerialPortException e) {
                throw new IOException("Failed to close serial port", e);
            } finally {
                port = null;
            }
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
                return SerialPort.PARITY_EVEN;
            case 'O':
                return SerialPort.PARITY_ODD;
            case 'M':
                return SerialPort.PARITY_MARK;
            case 'S':
                return SerialPort.PARITY_SPACE;
            default:
                return SerialPort.PARITY_NONE;
        }
    }

    private static int mapStopBits(float stopBits) {
        if (Float.compare(stopBits, 1.5f) == 0) return SerialPort.STOPBITS_1_5;
        if (Float.compare(stopBits, 2.0f) == 0) return SerialPort.STOPBITS_2;
        return SerialPort.STOPBITS_1;
    }
}
